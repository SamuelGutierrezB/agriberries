package com.agriberriesmx.agriberries.Utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.agriberriesmx.agriberries.POJO.Client;
import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Item;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Part;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AgronomistAI {

    public interface RecommendationCallback {
        void onSuccess(String recommendation);

        void onFailure(Exception e);
    }

    public static void getAgronomistRecommendation(Context context, String clientId, String unitId, String diagnosticId, @NonNull final RecommendationCallback callback) {
        // Create instance of Executor and Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Executor executor = Executors.newSingleThreadExecutor();

        // Create tasks to fetch data from Firestore
        Task<DocumentSnapshot> clientTask = db.collection("clients").document(clientId).get();

        Task<DocumentSnapshot> unitTask = db.collection("clients").document(clientId)
                .collection("units").document(unitId).get();

        Task<DocumentSnapshot> diagnosticTask = db.collection("clients").document(clientId)
                .collection("diagnostics").document(diagnosticId).get();

        Task<QuerySnapshot> itemsTask = db.collection("clients").document(clientId)
                .collection("diagnostics").document(diagnosticId).collection("items").get();

        // Combine the tasks into a single task
        Task<List<Object>> allTasks = Tasks.whenAllSuccess(clientTask, unitTask, diagnosticTask, itemsTask);

        allTasks.addOnSuccessListener(executor, results -> {
            try {
                // Process the results from Firestore
                DocumentSnapshot clientSnapshot = (DocumentSnapshot) results.get(0);
                DocumentSnapshot unitSnapshot = (DocumentSnapshot) results.get(1);
                DocumentSnapshot diagnosticSnapshot = (DocumentSnapshot) results.get(2);
                QuerySnapshot itemsSnapshot = (QuerySnapshot) results.get(3);

                if (!clientSnapshot.exists()) {
                    callback.onFailure(new Exception("The document of the client does not exist: " + clientId));
                    return;
                }
                if (!unitSnapshot.exists()) {
                    callback.onFailure(new Exception("The document of the unit does not exist: " + unitId));
                    return;
                }
                if (!diagnosticSnapshot.exists()) {
                    callback.onFailure(new Exception("The document of the diagnostic does not exist: " + diagnosticId));
                    return;
                }
                if (itemsSnapshot.isEmpty()) {
                    callback.onFailure(new Exception("Items not found for diagnostic: " + diagnosticId));
                    return;
                }

                // Convert snapshots to objects
                Client client = clientSnapshot.toObject(Client.class);
                Unit unit = unitSnapshot.toObject(Unit.class);
                Diagnostic diagnostic = diagnosticSnapshot.toObject(Diagnostic.class);
                List<Item> items = itemsSnapshot.toObjects(Item.class);

                if (client != null && unit != null && diagnostic != null && !items.isEmpty()) {
                    // Create prompt from data
                    Content content = buildPromptFromData(context, client, unit, diagnostic, items);

                    // Call Gemini API
                    callGeminiAPI(context, content, executor, callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing data from Firestore", e);
                callback.onFailure(e);
            }
        }).addOnFailureListener(executor, e -> {
            Log.e(TAG, "Error trying to fetch data from Firestore", e);
            callback.onFailure(e);
        });
    }

    private static Content buildPromptFromData(Context context, Client client, Unit unit, Diagnostic diagnostic, List<Item> items) {
        // Create prompt from data
        StringBuilder prompt = new StringBuilder();

        // Get frequencies and specific frequency from the client
        String[] frequencies = context.getResources().getStringArray(R.array.frequencies);
        int frequencyIndex = client.getFrequency();

        // Get frequency word
        String frequencyWord = "SEMANAL";
        if (frequencyIndex >= 0 && frequencyIndex < frequencies.length)
            frequencyWord = frequencies[frequencyIndex];

        // Rol and context for AI
        prompt.append("Actúa como un asesor agrónomo experto en el cultivo de ")
                .append(unit.getCrop())
                .append(", con especialidad en manejo ")
                .append(unit.getManagement())
                .append(". Genera una recomendación técnica detallada y un plan de acción ").append(frequencyWord).append(".\n\n");

        // Production Unit Information
        prompt.append("--- DATOS DE LA UNIDAD DE PRODUCCIÓN ---\n");
        prompt.append("- Cultivo: ").append(unit.getCrop()).append("\n");
        prompt.append("- Hectáreas: ").append(unit.getHectares()).append("\n");
        prompt.append("- Tipo de Manejo: ").append(unit.getManagement()).append("\n");
        prompt.append("- Ubicación: ").append(unit.getLocation()).append("\n\n");

        // General Observations from Diagnostic
        prompt.append("--- OBSERVACIONES GENERALES DEL DIAGNÓSTICO ---\n");
        prompt.append("- Observaciones del Consultor: ").append(diagnostic.getObservations()).append("\n\n");

        // Items Information
        prompt.append("--- HALLAZGOS ESPECÍFICOS POR PUNTO DE MUESTREO ---\n");
        Set<String> allPlagues = new HashSet<>();
        Set<String> allDeficiencies = new HashSet<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            prompt.append("\n[Punto de Muestreo ").append(i + 1).append("]\n");
            prompt.append("- Fenología de la Planta: ").append(item.getPhenology()).append("\n");
            if (item.getPlagues() != null && !item.getPlagues().isEmpty()) {
                prompt.append("- Plagas Detectadas: ").append(String.join(", ", item.getPlagues())).append("\n");
                allPlagues.addAll(item.getPlagues());
            }
            if (item.getDeficiencies() != null && !item.getDeficiencies().isEmpty()) {
                prompt.append("- Deficiencias Detectadas: ").append(String.join(", ", item.getDeficiencies())).append("\n");
                allDeficiencies.addAll(item.getDeficiencies());
            }
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                prompt.append("- Nota Adicional del Punto: ").append(item.getNote()).append("\n");
            }
        }

        prompt.append("\n--- RESUMEN DE PROBLEMAS A ATENDER ---\n");
        if (!allPlagues.isEmpty()) {
            prompt.append("- Plagas Principales (el número en paréntesis es la intensidad de 1 a 5): ").append(String.join(", ", allPlagues)).append("\n");
        } else {
            prompt.append("- No se reportaron plagas significativas.\n");
        }
        if (!allDeficiencies.isEmpty()) {
            prompt.append("- Deficiencias Nutricionales (el número en paréntesis es la intensidad de 1 a 5): ").append(String.join(", ", allDeficiencies)).append("\n");
        } else {
            prompt.append("- No se reportaron deficiencias nutricionales.\n");
        }

        // Specific task for AI
        prompt.append("\n--- TAREA Y ESTRUCTURA DE SALIDA REQUERIDA ---\n");
        prompt.append("Basado en todos los datos proporcionados, genera una recomendación técnica. La respuesta debe ser concisa, directa y estructurada EXACTAMENTE en las siguientes tres secciones, usando los títulos exactos que te proporciono. No añadas introducciones, resúmenes ni texto fuera de estas secciones. No uses viñetas, asteriscos ni guiones.\n\n");

        prompt.append("## 1. MANEJO DE POBLACIONES (Plagas y Enfermedades)\n");
        prompt.append("Genera una lista de aplicaciones. Para cada una indica: Ubicación (el sector de la huerta), Producto, Dosis (por hectárea o por volumen de agua), y una Nota breve (ej. 'vs trips', '3 dias despues de la #1').\n\n");

        prompt.append("## 2. PLAN DE FERTIRRIEGO (Nutrición y Agua)\n");
        prompt.append("Genera un plan de fertirriego. Para cada producto indica: Nombre del Producto, Dosis Total (en kg/ha o lt/ha para el periodo), e Instrucciones de Aplicación (ej. 'En 2 aplicaciones', 'Aplicar en seco', 'Inyectar a pH 5.8').\n\n");

        prompt.append("## 3. ACCIONES DE MANEJO (Actividades Culturales)\n");
        prompt.append("Genera una lista numerada de acciones o actividades culturales específicas a realizar, como podas, deshojes, manejos de suelo, etc.\n\n");

        // Get crop name from unit and convert to lowercase and remove non-alphabetic characters
        String cropFileName = unit.getCrop().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        int resourceId = context.getResources().getIdentifier(cropFileName, "raw", context.getPackageName());

        // Read PDF file from resources
        byte[] pdfBytes = null;
        if (resourceId != 0) {
            try {
                // Read the PDF file from resources
                InputStream inputStream = context.getResources().openRawResource(resourceId);
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] chunk = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(chunk)) != -1) {
                    byteStream.write(chunk, 0, bytesRead);
                }
                pdfBytes = byteStream.toByteArray();
                inputStream.close();

                prompt.append("\n--- INSTRUCCIÓN ESPECIAL ---\n");
                prompt.append("Para la sección 'Manejo de Plagas y Enfermedades', basa tus recomendaciones de productos ÚNICAMENTE en la lista proporcionada en el documento PDF adjunto. No sugieras productos que no estén en ese archivo.\n");

            } catch (Exception e) {
                // Handle exceptions
                Log.e(TAG, "Error reading PDF file", e);
                pdfBytes = null;
            }
        }

        // Log prompt for debugging
        Log.d(TAG, "Prompt Generado: " + prompt);

        // Create Content object for Gemini API
        Content.Builder contentBuilder = new Content.Builder();
        contentBuilder.addText(prompt.toString());

        if (pdfBytes != null) {
            // Add PDF as part
            contentBuilder.addBlob("application/pdf", pdfBytes);
            Log.d(TAG, "PDF para el cultivo '" + cropFileName + "' adjuntado a la solicitud.");
        }

        return contentBuilder.build();
    }

    private static void callGeminiAPI(Context context, Content content, Executor executor, @NonNull final RecommendationCallback callback) {
        // Generative AI API call
        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                context.getString(R.string.gemini_api_key)
        );
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Execute the API call
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String recommendationText = result.getText();
                if (recommendationText != null) {
                    callback.onSuccess(recommendationText);
                } else {
                    callback.onFailure(new Exception("The recommendation text is null."));
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "Error trying to call Gemini API", t);
                callback.onFailure(new Exception(t));
            }
        }, executor);
    }

}
