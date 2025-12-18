package com.agriberriesmx.agriberries.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;

import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.agriberriesmx.agriberries.POJO.Block;
import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Item;
import com.agriberriesmx.agriberries.POJO.Nutrient;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.R;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PdfGenerator {

        public static void generatePdf(Context context, Diagnostic diagnostic, Unit unit, List<Block> blocks,
                        List<Item> items, List<Nutrient> nutrients, String dest) throws Exception {
                // Initialize PDF writer
                PdfWriter writer = new PdfWriter(dest);

                // Sort items list by phenology ascending
                items.sort(Comparator.comparing(Item::getPhenology));

                // Create map to get [row, col] from sorted items
                Map<String, Integer> itemPositionToIndex = new HashMap<>();
                for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);
                        String key = item.getRow() + "," + item.getCol();
                        itemPositionToIndex.put(key, i);
                }

                // Sort blocks list based on [row, col] from items list
                blocks.sort((block1, block2) -> {
                        String key1 = block1.getRow() + "," + block1.getCol();
                        String key2 = block2.getRow() + "," + block2.getCol();
                        return Integer.compare(itemPositionToIndex.getOrDefault(key1, -1),
                                        itemPositionToIndex.getOrDefault(key2, -1));
                });

                // Initialize PDF document
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add title to the document
                Paragraph title = new Paragraph(context.getResources().getString(R.string.pdf_header_diagnostic))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontSize(20)
                                .setBold()
                                .setMarginTop(30);

                // Get logo image
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_logo);
                Bitmap bitmap;
                if (drawable != null) {
                        bitmap = ((BitmapDrawable) drawable).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        ImageData data = ImageDataFactory.create(byteArray);
                        Image img = new Image(data);

                        // Decrease size of the image
                        float desiredWidth = 150;
                        float aspectRatio = img.getImageWidth() / img.getImageHeight();
                        img.scaleAbsolute(desiredWidth, desiredWidth / aspectRatio);

                        document.add(img);
                }

                // Add title and image to the document
                document.add(title);

                // Create simple date format
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                // Get consultant name and show it
                SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
                String consultantName = sharedPreferencesManager.getNameFromPreferences();

                // Create paragraph with consultant name
                Paragraph consultantParagraph = new Paragraph()
                                .add(new Text(context.getResources().getString(R.string.pdf_consultant)).setBold())
                                .add(new Text(consultantName))
                                .setTextAlignment(TextAlignment.LEFT);

                // Create general information table
                Table tableGeneralInfo = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create general information table header
                String generalInformationHeader = context.getResources()
                                .getString(R.string.pdf_header_general_information);
                Cell cellGeneralInformationHeader = new Cell().add(new Paragraph(generalInformationHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                tableGeneralInfo.addCell(cellGeneralInformationHeader);

                // Create second row of general information table
                String[] secondTitles = {
                                context.getResources().getString(R.string.pdf_ranch), unit.getName(),
                                context.getResources().getString(R.string.pdf_location), unit.getLocation(),
                                context.getResources().getString(R.string.pdf_crop), unit.getCrop(),
                                context.getResources().getString(R.string.pdf_date), sdf.format(new Date())
                };
                Table secondTableInfo = new Table(UnitValue.createPercentArray(
                                new float[] { 0.100f, 0.150f, 0.100f, 0.150f, 0.100f, 0.150f, 0.100f, 0.150f }))
                                .useAllAvailableWidth();

                for (int counter = 0; counter < secondTitles.length; counter++) {
                        Paragraph info = new Paragraph(secondTitles[counter]);
                        if (counter % 2 == 0)
                                info.setBold();

                        // Create cell with center alignment
                        Cell cell = new Cell().add(info).setVerticalAlignment(VerticalAlignment.MIDDLE);
                        info.setTextAlignment(TextAlignment.CENTER);
                        secondTableInfo.addCell(cell);
                }

                // Get instance of current time and get week number and year
                Calendar calendar = Calendar.getInstance();
                int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
                int year = calendar.get(Calendar.YEAR);

                // Create third row of general information table
                String[] thirdTitles = {
                                context.getResources().getString(R.string.pdf_system),
                                unit.getManagement() + "-" + unit.getSoil(),
                                context.getResources().getString(R.string.pdf_altitude),
                                String.valueOf(unit.getAltitude()),
                                context.getResources().getString(R.string.pdf_hectares),
                                String.valueOf(unit.getHectares()),
                                context.getResources().getString(R.string.pdf_week),
                                weekOfYear + "." + String.valueOf(year).substring(2)
                };
                Table thirdTableInfo = new Table(UnitValue.createPercentArray(
                                new float[] { 0.100f, 0.150f, 0.100f, 0.150f, 0.100f, 0.150f, 0.100f, 0.150f }))
                                .useAllAvailableWidth();

                for (int counter = 0; counter < thirdTitles.length; counter++) {
                        Paragraph info = new Paragraph(thirdTitles[counter]);
                        if (counter % 2 == 0)
                                info.setBold();

                        // Create cell with center alignment
                        Cell cell = new Cell().add(info).setVerticalAlignment(VerticalAlignment.MIDDLE);
                        info.setTextAlignment(TextAlignment.CENTER);
                        thirdTableInfo.addCell(cell);
                }

                // Create phenology table
                Table phenologyTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create phenology table header
                String phenologyHeader = context.getResources().getString(R.string.pdf_header_phenology);
                Cell cellPhenologyHeader = new Cell().add(new Paragraph(phenologyHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                phenologyTable.addCell(cellPhenologyHeader);

                // Create second phenology table
                Table secondPhenologyTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.20f, 0.18f, 0.50f }))
                                .useAllAvailableWidth();

                // Create second phenology table header
                String[] secondPhenologyHeader = { context.getResources().getString(R.string.headerLocation),
                                context.getResources().getString(R.string.headerVariety),
                                context.getResources().getString(R.string.headerPhenology),
                                context.getResources().getString(R.string.headerDescription) };
                for (String header : secondPhenologyHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondPhenologyTable.addCell(cell);
                }

                // Create third phenology table
                Table thirdPhenologyTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.20f, 0.18f, 0.50f }))
                                .useAllAvailableWidth();

                // Create third phenology table info
                int position = 0;
                for (Block block : blocks) {
                        // Add block name
                        Cell nameCell = new Cell()
                                        .add(new Paragraph(block.getName()))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        thirdPhenologyTable.addCell(nameCell);

                        // Add block type
                        Cell typeCell = new Cell()
                                        .add(new Paragraph(block.getType()))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        thirdPhenologyTable.addCell(typeCell);

                        // Add block phenology
                        Cell dateCell = new Cell()
                                        .add(new Paragraph(items.get(position).getPhenology()))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        thirdPhenologyTable.addCell(dateCell);

                        // Create simple date format
                        SimpleDateFormat formatter = new SimpleDateFormat("d 'de' MMMM 'de' yyyy",
                                        new Locale("es", "MX"));

                        // Create block description
                        Item item = items.get(position);
                        String description = "";
                        if (!item.getPhenology().equals(context.getResources().getString(R.string.done))) {
                                description += context.getResources().getString(R.string.descriptionPlantationDate);
                                description += " " + formatter.format(block.getPlantationDate()) + "\n";
                                description += context.getResources().getString(R.string.descriptionHeight);
                                description += " " + item.getHeight() + " cm\n";
                                description += context.getResources().getString(R.string.descriptionLongitude);
                                description += " " + item.getLongitude() + " cm\n";
                                description += context.getResources().getString(R.string.descriptionWeeklyGrowing);
                                description += " " + item.getWeeklyGrowing() + " cm\n";

                                // Add block notes if they exist
                                if (item.getNote() != null && !item.getNote().trim().isEmpty()) {
                                        description += "\nNota de Bloque: " + item.getNote();
                                }
                        }

                        // Add block description
                        Cell descriptionCell = new Cell()
                                        .add(new Paragraph(description))
                                        .setTextAlignment(TextAlignment.JUSTIFIED)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        thirdPhenologyTable.addCell(descriptionCell);

                        // Increment position
                        position++;
                }

                // Create chart of blocks height
                boolean avocado = unit.getCrop().equals(context.getResources().getString(R.string.avocado));
                Bitmap chartBitmap = createBarChartBitmap(context, blocks, items, avocado);

                // Convert Bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Image chartImage = new Image(ImageDataFactory.create(byteArray));

                // Create plague table
                Table plagueTable = new Table(UnitValue.createPercentArray(new float[] { 1 })).useAllAvailableWidth();

                // Create plague table header
                String plagueHeader = context.getResources().getString(R.string.pdf_header_plagues);
                Cell cellPlagueHeader = new Cell().add(new Paragraph(plagueHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                plagueTable.addCell(cellPlagueHeader);

                // Create second plague table
                Table secondPlagueTable = new Table(UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create second plague table header
                String[] secondPlagueTableHeader = { context.getResources().getString(R.string.headerLocation),
                                context.getResources().getString(R.string.headerOrganism),
                                context.getResources().getString(R.string.headerIntensity) };
                for (String header : secondPlagueTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondPlagueTable.addCell(cell);
                }

                // Create third plague table
                Table thirdPlagueTable = new Table(UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create third plague table info
                position = 0;
                for (Item item : items) {
                        if (!item.getPhenology().equals(context.getResources().getString(R.string.done))) {
                                // Get plague list
                                List<String> plagues = item.getPlagues();

                                // Calculate number of row for this item
                                int numberOfRows = plagues.size();
                                if (numberOfRows == 0) {
                                        plagues.add("N/A(0)");
                                        numberOfRows = 1;
                                }

                                // Add block name
                                Cell nameCell = new Cell(numberOfRows, 1)
                                                .add(new Paragraph(blocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdPlagueTable.addCell(nameCell);

                                // Iteration on plague list and add each plague to a new row
                                for (String plague : plagues) {
                                        // Get description and level of the plague
                                        String description = plague.replaceAll("\\(\\d+\\)$", "").trim();
                                        String number = plague.replaceAll(".*\\((\\d+)\\)$", "$1").trim();
                                        int intensity = Integer.parseInt(number);

                                        // Add plague description
                                        Cell descriptionCell = new Cell()
                                                        .add(new Paragraph(description))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                        thirdPlagueTable.addCell(descriptionCell);

                                        // Add level of the plague
                                        Cell numberCell = new Cell()
                                                        .add(new Paragraph(number))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                        .setBackgroundColor(Formatting.getColorForIntensity(intensity));
                                        thirdPlagueTable.addCell(numberCell);
                                }
                        }

                        // Increment position
                        position++;
                }

                // Create deficiency table
                Table deficiencyTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create deficiency table header
                String deficiencyHeader = context.getResources().getString(R.string.pdf_header_deficiencies);
                Cell cellDeficiencyHeader = new Cell().add(new Paragraph(deficiencyHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                deficiencyTable.addCell(cellDeficiencyHeader);

                // Create second deficiency table
                Table secondDeficiencyTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create second deficiency table header
                String[] secondDeficiencyTableHeader = { context.getResources().getString(R.string.headerLocation),
                                context.getResources().getString(R.string.headerDeficiency),
                                context.getResources().getString(R.string.headerIntensity) };
                for (String header : secondDeficiencyTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondDeficiencyTable.addCell(cell);
                }

                // Create third deficiency table
                Table thirdDeficiencyTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create third deficiency table info
                position = 0;
                for (Item item : items) {
                        if (!item.getPhenology().equals(context.getResources().getString(R.string.done))) {
                                // Get deficiencies list
                                List<String> deficiencies = item.getDeficiencies();

                                // Calculate number of row for this item
                                int numberOfRows = deficiencies.size();
                                if (numberOfRows == 0) {
                                        deficiencies.add("N/A(0)");
                                        numberOfRows = 1;
                                }

                                // Add block name
                                Cell nameCell = new Cell(numberOfRows, 1)
                                                .add(new Paragraph(blocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdDeficiencyTable.addCell(nameCell);

                                // Iteration on deficiency list and add each deficiency to a new row
                                for (String deficiency : deficiencies) {
                                        // Get description and level of the deficiency
                                        String description = deficiency.replaceAll("\\(\\d+\\)$", "").trim();
                                        String number = deficiency.replaceAll(".*\\((\\d+)\\)$", "$1").trim();
                                        int intensity = Integer.parseInt(number);

                                        // Add deficiency description
                                        Cell descriptionCell = new Cell()
                                                        .add(new Paragraph(description))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                        thirdDeficiencyTable.addCell(descriptionCell);

                                        // Add level of the deficiency
                                        Cell numberCell = new Cell()
                                                        .add(new Paragraph(number))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                        .setBackgroundColor(Formatting.getColorForIntensity(intensity));
                                        thirdDeficiencyTable.addCell(numberCell);
                                }
                        }

                        // Increment position
                        position++;
                }

                // Create contingency table
                Table contingencyTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create contingency table header
                String contingencyHeader = context.getResources().getString(R.string.pdf_header_contingencies);
                Cell cellContingencyHeader = new Cell().add(new Paragraph(contingencyHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                contingencyTable.addCell(cellContingencyHeader);

                // Create second contingency table
                Table secondContingencyTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create second contingency table header
                String[] secondContingencyTableHeader = { context.getResources().getString(R.string.headerLocation),
                                context.getResources().getString(R.string.headerContingency),
                                context.getResources().getString(R.string.headerIntensity) };
                for (String header : secondContingencyTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondContingencyTable.addCell(cell);
                }

                // Create third contingency table
                Table thirdContingencyTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create third contingency table info
                position = 0;
                for (Item item : items) {
                        if (!item.getPhenology().equals(context.getResources().getString(R.string.done))) {
                                // Get contingencies list
                                List<String> contingencies = item.getContingencies();

                                // Calculate number of row for this item
                                int numberOfRows = contingencies.size();
                                if (numberOfRows == 0) {
                                        contingencies.add("N/A(0)");
                                        numberOfRows = 1;
                                }

                                // Add block name
                                Cell nameCell = new Cell(numberOfRows, 1)
                                                .add(new Paragraph(blocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdContingencyTable.addCell(nameCell);

                                // Iteration on contingency list and add each contingency to a new row
                                for (String contingency : contingencies) {
                                        // Get description and level of the contingency
                                        String description = contingency.replaceAll("\\(\\d+\\)$", "").trim();
                                        String number = contingency.replaceAll(".*\\((\\d+)\\)$", "$1").trim();
                                        int intensity = Integer.parseInt(number);

                                        // Add contingency description
                                        Cell descriptionCell = new Cell()
                                                        .add(new Paragraph(description))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                        thirdContingencyTable.addCell(descriptionCell);

                                        // Add level of the contingency
                                        Cell numberCell = new Cell()
                                                        .add(new Paragraph(number))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                        .setBackgroundColor(Formatting.getColorForIntensity(intensity));
                                        thirdContingencyTable.addCell(numberCell);
                                }
                        }

                        // Increment position
                        position++;
                }

                // Create activity table
                Table activityTable = new Table(UnitValue.createPercentArray(new float[] { 1 })).useAllAvailableWidth();

                // Create activity table header
                String activityHeader = context.getResources().getString(R.string.pdf_header_activities);
                Cell cellActivityHeader = new Cell().add(new Paragraph(activityHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                activityTable.addCell(cellActivityHeader);

                // Create second activity table
                Table secondActivityTable = new Table(UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create second activity table header
                String[] secondActivityTableHeader = { context.getResources().getString(R.string.headerLocation),
                                context.getResources().getString(R.string.headerActivity),
                                context.getResources().getString(R.string.headerProgress) };
                for (String header : secondActivityTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondActivityTable.addCell(cell);
                }

                // Create third activity table
                Table thirdActivityTable = new Table(UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create third activity table info
                position = 0;
                for (Item item : items) {
                        if (!item.getPhenology().equals(context.getResources().getString(R.string.done))) {
                                // Get activities list
                                List<String> activities = item.getActivities();

                                // Calculate number of row for this item
                                int numberOfRows = activities.size();
                                if (numberOfRows == 0) {
                                        activities.add("N/A(0)");
                                        numberOfRows = 1;
                                }

                                // Add block name
                                Cell nameCell = new Cell(numberOfRows, 1)
                                                .add(new Paragraph(blocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdActivityTable.addCell(nameCell);

                                // Iteration on activity list and add each activity to a new row
                                for (String activity : activities) {
                                        // Get description and level of the activity
                                        String description = activity.replaceAll("\\(.*\\)$", "").trim();
                                        String number = activity.replaceAll(".*\\((\\d+)%?\\).*", "$1").trim();
                                        int percentage = Integer.parseInt(number);

                                        // Add activity description
                                        Cell descriptionCell = new Cell()
                                                        .add(new Paragraph(description))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                        thirdActivityTable.addCell(descriptionCell);

                                        // Progress bar configuration
                                        Table progressBar = new Table(UnitValue.createPercentArray(
                                                        new float[] { percentage, 100 - percentage }));
                                        progressBar.useAllAvailableWidth();

                                        // Progress cell
                                        Cell progressCell = new Cell()
                                                        .add(new Paragraph(""))
                                                        .setBackgroundColor(
                                                                        Formatting.getColorForPercentage(percentage))
                                                        .setHeight(10)
                                                        .setPadding(0);
                                        progressBar.addCell(progressCell);

                                        // Remaining cell
                                        Cell remainingCell = new Cell()
                                                        .add(new Paragraph(""))
                                                        .setBackgroundColor(new DeviceRgb(255, 255, 255))
                                                        .setHeight(10)
                                                        .setPadding(0);
                                        progressBar.addCell(remainingCell);

                                        // Create a cell to contains the progress bar and its percentage
                                        Cell numberCell = new Cell()
                                                        .setPadding(0)
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);

                                        // Add the progress bar first to the cell
                                        numberCell.add(progressBar);

                                        // Add percentage text at the bottom of the cell
                                        Paragraph percentageText = new Paragraph(number + "%")
                                                        .setTextAlignment(TextAlignment.CENTER);
                                        numberCell.add(percentageText);

                                        // Add the modified cell to the table
                                        thirdActivityTable.addCell(numberCell);
                                }
                        }

                        // Increment position
                        position++;
                }

                // Create task table
                Table taskTable = new Table(UnitValue.createPercentArray(new float[] { 1 })).useAllAvailableWidth();

                // Create task table header
                String taskHeader = context.getResources().getString(R.string.pdf_header_tasks);
                Cell cellTaskHeader = new Cell().add(new Paragraph(taskHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                taskTable.addCell(cellTaskHeader);

                // Create second task table
                Table secondTaskTable = new Table(UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create second task table header
                String[] secondTaskTableHeader = { context.getResources().getString(R.string.headerLocation),
                                context.getResources().getString(R.string.headerTask),
                                context.getResources().getString(R.string.headerProgress) };
                for (String header : secondTaskTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondTaskTable.addCell(cell);
                }

                // Create third task table
                Table thirdTaskTable = new Table(UnitValue.createPercentArray(new float[] { 0.12f, 0.63f, 0.25f }))
                                .useAllAvailableWidth();

                // Create third task table info
                position = 0;
                for (Item item : items) {
                        if (!item.getPhenology().equals(context.getResources().getString(R.string.done))) {
                                // Get tasks list
                                List<String> tasks = item.getTasks();

                                // Calculate number of row for this item
                                int numberOfRows = tasks.size();
                                if (numberOfRows == 0) {
                                        tasks.add("N/A(0)");
                                        numberOfRows = 1;
                                }

                                // Add block name
                                Cell nameCell = new Cell(numberOfRows, 1)
                                                .add(new Paragraph(blocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdTaskTable.addCell(nameCell);

                                // Iteration on task list and add each task to a new row
                                for (String task : tasks) {
                                        // Get description and level of the task
                                        String description = task.replaceAll("\\(.*\\)$", "").trim();
                                        String number = task.replaceAll(".*\\((\\d+)%?\\).*", "$1").trim();
                                        int percentage = Integer.parseInt(number);

                                        // Add task description
                                        Cell descriptionCell = new Cell()
                                                        .add(new Paragraph(description))
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                        thirdTaskTable.addCell(descriptionCell);

                                        // Progress bar configuration
                                        Table progressBar = new Table(UnitValue.createPercentArray(
                                                        new float[] { percentage, 100 - percentage }));
                                        progressBar.useAllAvailableWidth();

                                        // Progress cell
                                        Cell progressCell = new Cell()
                                                        .add(new Paragraph(""))
                                                        .setBackgroundColor(
                                                                        Formatting.getColorForPercentage(percentage))
                                                        .setHeight(10)
                                                        .setPadding(0);
                                        progressBar.addCell(progressCell);

                                        // Remaining cell
                                        Cell remainingCell = new Cell()
                                                        .add(new Paragraph(""))
                                                        .setBackgroundColor(new DeviceRgb(255, 255, 255))
                                                        .setHeight(10)
                                                        .setPadding(0);
                                        progressBar.addCell(remainingCell);

                                        // Create a cell to contains the progress bar and its percentage
                                        Cell numberCell = new Cell()
                                                        .setPadding(0)
                                                        .setTextAlignment(TextAlignment.CENTER)
                                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);

                                        // Add the progress bar first to the cell
                                        numberCell.add(progressBar);

                                        // Add percentage text at the bottom of the cell
                                        Paragraph percentageText = new Paragraph(number + "%")
                                                        .setTextAlignment(TextAlignment.CENTER);
                                        numberCell.add(percentageText);

                                        // Add cell to the table
                                        thirdTaskTable.addCell(numberCell);
                                }
                        }

                        // Increment position
                        position++;
                }

                // Create harvest table
                Table harvestTable = new Table(UnitValue.createPercentArray(new float[] { 1 })).useAllAvailableWidth();

                // Create harvest table header
                String harvestHeader = context.getResources().getString(R.string.pdf_header_harvest);
                Cell cellHarvestHeader = new Cell().add(new Paragraph(harvestHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                harvestTable.addCell(cellHarvestHeader);

                // Create second harvest table
                Table secondHarvestTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.18f, 0.17f, 0.18f, 0.17f, 0.18f }))
                                .useAllAvailableWidth();

                // Create second harvest table header
                String[] secondHarvestTableHeader = { context.getResources().getString(R.string.headerLocation),
                                context.getResources().getString(R.string.header_size),
                                context.getResources().getString(R.string.header_fruit_per_plant_meter),
                                context.getResources().getString(R.string.header_gram_per_fruit),
                                context.getResources().getString(R.string.header_kilogram_per_hectare),
                                context.getResources().getString(R.string.header_efficiency) };
                for (String header : secondHarvestTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondHarvestTable.addCell(cell);
                }

                // Create third task table
                Table thirdHarvestTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.12f, 0.18f, 0.17f, 0.18f, 0.17f, 0.18f }))
                                .useAllAvailableWidth();

                // Create third task table info
                boolean isHarvestInfo = false;
                position = 0;
                for (Item item : items) {
                        if (item.getPhenology().contains(context.getResources().getString(R.string.harvest_text))) {
                                // Change state
                                if (!isHarvestInfo)
                                        isHarvestInfo = true;

                                // Add block name
                                Block block = blocks.get(position);
                                Cell nameCell = new Cell(1, 1)
                                                .add(new Paragraph(block.getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdHarvestTable.addCell(nameCell);

                                // Add fruit size
                                Cell sizeCell = new Cell(1, 1)
                                                .add(new Paragraph(item.getFruitSize() != null ? item.getFruitSize()
                                                                : "NA"))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdHarvestTable.addCell(sizeCell);

                                // Add fruit per plant meter
                                Cell fruitPerPlantMeterCell = new Cell(1, 1)
                                                .add(new Paragraph(String.valueOf(item.getFruitPerPlantMeter())))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdHarvestTable.addCell(fruitPerPlantMeterCell);

                                // Add gram per fruit
                                Cell gramPerFruitCell = new Cell(1, 1)
                                                .add(new Paragraph(String.valueOf(item.getGramPerFruit())))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdHarvestTable.addCell(gramPerFruitCell);

                                // Calculate ratio
                                double ratio;
                                if (unit.getCrop().equals(context.getResources().getString(R.string.blackberry)) ||
                                                unit.getCrop().equals(
                                                                context.getResources().getString(R.string.raspberry))) {
                                        ratio = (100 / block.getFurrowDistance()) * 100 * 2;
                                } else
                                        ratio = 10000 / (block.getFurrowDistance() * block.getPlantDistance());
                                ratio = Math.round(ratio);

                                // Calculate kilogram per hectare
                                double kilogramPerHectare = (ratio * item.getFruitPerPlantMeter()
                                                * item.getGramPerFruit()) / 1000;
                                kilogramPerHectare = kilogramPerHectare
                                                * (1 - (double) (item.getShrinkagePercentage()) / 100);

                                // Add kilogram per hectare
                                Cell kilogramPerPlantCell = new Cell(1, 1)
                                                .add(new Paragraph(String.format(Locale.getDefault(), "%,d",
                                                                (int) Math.round(kilogramPerHectare))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdHarvestTable.addCell(kilogramPerPlantCell);

                                // Add efficiency
                                Cell efficiencyCell = new Cell(1, 1)
                                                .add(new Paragraph((100 - item.getShrinkagePercentage()) + "%"))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdHarvestTable.addCell(efficiencyCell);
                        }

                        // Increment position
                        position++;
                }

                // Create nutrients blocks
                List<Block> copiedBlocks = new ArrayList<>(blocks);
                List<Block> nutrientsBlocks = Formatting.getSortedBlockList(copiedBlocks);

                // Create nutrients (dropper) table
                Table nutrientsDropperTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create nutrients (dropper) table header
                String nutrientsDropperHeader = context.getResources().getString(R.string.pdf_header_nutrients_dropper);
                Cell cellNutrientsDropperHeader = new Cell().add(new Paragraph(nutrientsDropperHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                nutrientsDropperTable.addCell(cellNutrientsDropperHeader);

                // Create second nutrients dropper table
                Table secondNutrientsDropperTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();

                // Create second nutrients dropper table header
                String[] secondNutrientsDropperTableHeader = { context.getResources().getString(R.string.headerBlock),
                                context.getResources().getString(R.string.headerNitrateSymbol),
                                context.getResources().getString(R.string.headerCalciumSymbol),
                                context.getResources().getString(R.string.headerSodiumSymbol),
                                context.getResources().getString(R.string.headerPotassiumSymbol),
                                context.getResources().getString(R.string.headerPhSymbol),
                                context.getResources().getString(R.string.headerConductivitySymbol) };
                for (String header : secondNutrientsDropperTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondNutrientsDropperTable.addCell(cell);
                }

                // Create third nutrients dropper table
                Table thirdNutrientsDropperTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();
                List<Nutrient> nutrientList = Formatting.getSortedNutrientList(nutrients);

                // Get boolean to know if nutrients dropper is empty
                boolean isNutrientsDropperEmpty = true;
                for (Nutrient nutrient : nutrientList) {
                        if (isNutrientsDropperEmpty) {
                                if (nutrient.getNitrate().get(0) != 0)
                                        isNutrientsDropperEmpty = false;
                                if (nutrient.getCalcium().get(0) != 0)
                                        isNutrientsDropperEmpty = false;
                                if (nutrient.getSodium().get(0) != 0)
                                        isNutrientsDropperEmpty = false;
                                if (nutrient.getPotassium().get(0) != 0)
                                        isNutrientsDropperEmpty = false;
                                if (nutrient.getPh().get(0) != 0.0)
                                        isNutrientsDropperEmpty = false;
                                if (nutrient.getConductivity().get(0) != 0.0)
                                        isNutrientsDropperEmpty = false;
                        } else
                                break;
                }

                if (!isNutrientsDropperEmpty) {
                        // Create third nutrients dropper table info
                        position = 0;
                        for (Nutrient nutrient : nutrientList) {
                                // Add block name
                                Cell nameCell = new Cell()
                                                .add(new Paragraph(nutrientsBlocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsDropperTable.addCell(nameCell);

                                // Add nitrate info
                                Cell nitrateCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getNitrate().get(0))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getNitrate().get(0), 0));
                                thirdNutrientsDropperTable.addCell(nitrateCell);

                                // Add calcium info
                                Cell calciumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getCalcium().get(0))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getCalcium().get(0), 0));
                                thirdNutrientsDropperTable.addCell(calciumCell);

                                // Add sodium info
                                Cell sodiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getSodium().get(0))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getSodium().get(0), 1));
                                thirdNutrientsDropperTable.addCell(sodiumCell);

                                // Add potassium info
                                Cell potassiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPotassium().get(0))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getPotassium().get(0), 0));
                                thirdNutrientsDropperTable.addCell(potassiumCell);

                                // Add ph info
                                Cell phCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPh().get(0))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getPh().get(0), 2));
                                thirdNutrientsDropperTable.addCell(phCell);

                                // Add conductivity info
                                Cell conductivityCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getConductivity().get(0))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsDropperTable.addCell(conductivityCell);

                                // Increment position
                                position++;
                        }
                }

                // Create nutrients (drainage) table
                Table nutrientsDrainageTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create nutrients (drainage) table header
                String nutrientsDrainageHeader = context.getResources()
                                .getString(R.string.pdf_header_nutrients_drainage);
                Cell cellNutrientsDrainageHeader = new Cell().add(new Paragraph(nutrientsDrainageHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                nutrientsDrainageTable.addCell(cellNutrientsDrainageHeader);

                // Create second nutrients drainage table
                Table secondNutrientsDrainageTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();

                // Create second nutrients drainage table header
                String[] secondNutrientsDrainageTableHeader = { context.getResources().getString(R.string.headerBlock),
                                context.getResources().getString(R.string.headerNitrateSymbol),
                                context.getResources().getString(R.string.headerCalciumSymbol),
                                context.getResources().getString(R.string.headerSodiumSymbol),
                                context.getResources().getString(R.string.headerPotassiumSymbol),
                                context.getResources().getString(R.string.headerPhSymbol),
                                context.getResources().getString(R.string.headerConductivitySymbol) };
                for (String header : secondNutrientsDrainageTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondNutrientsDrainageTable.addCell(cell);
                }

                // Create third nutrients drainage table
                Table thirdNutrientsDrainageTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();

                // Get boolean to know if nutrients drainage is empty
                boolean isNutrientsDrainageEmpty = true;
                for (Nutrient nutrient : nutrientList) {
                        if (isNutrientsDrainageEmpty) {
                                if (nutrient.getNitrate().get(1) != 0)
                                        isNutrientsDrainageEmpty = false;
                                if (nutrient.getCalcium().get(1) != 0)
                                        isNutrientsDrainageEmpty = false;
                                if (nutrient.getSodium().get(1) != 0)
                                        isNutrientsDrainageEmpty = false;
                                if (nutrient.getPotassium().get(1) != 0)
                                        isNutrientsDrainageEmpty = false;
                                if (nutrient.getPh().get(1) != 0.0)
                                        isNutrientsDrainageEmpty = false;
                                if (nutrient.getConductivity().get(1) != 0.0)
                                        isNutrientsDrainageEmpty = false;
                        } else
                                break;
                }

                if (!isNutrientsDrainageEmpty) {
                        // Create third nutrients drainage table info
                        position = 0;
                        for (Nutrient nutrient : nutrientList) {
                                // Add block name
                                Cell nameCell = new Cell()
                                                .add(new Paragraph(nutrientsBlocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsDrainageTable.addCell(nameCell);

                                // Add nitrate info
                                Cell nitrateCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getNitrate().get(1))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getNitrate().get(1), 0));
                                thirdNutrientsDrainageTable.addCell(nitrateCell);

                                // Add calcium info
                                Cell calciumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getCalcium().get(1))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getCalcium().get(1), 0));
                                thirdNutrientsDrainageTable.addCell(calciumCell);

                                // Add sodium info
                                Cell sodiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getSodium().get(1))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getSodium().get(1), 1));
                                thirdNutrientsDrainageTable.addCell(sodiumCell);

                                // Add potassium info
                                Cell potassiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPotassium().get(1))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getPotassium().get(1), 0));
                                thirdNutrientsDrainageTable.addCell(potassiumCell);

                                // Add ph info
                                Cell phCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPh().get(1))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getPh().get(1), 2));
                                thirdNutrientsDrainageTable.addCell(phCell);

                                // Add conductivity info
                                Cell conductivityCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getConductivity().get(1))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsDrainageTable.addCell(conductivityCell);

                                // Increment position
                                position++;
                        }
                }

                // Create nutrients (substratum) table
                Table nutrientsSubstratumTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create nutrients (substratum) table header
                String nutrientsSubstratumHeader = context.getResources()
                                .getString(R.string.pdf_header_nutrients_substratum);
                Cell cellNutrientsSubstratumHeader = new Cell().add(new Paragraph(nutrientsSubstratumHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                nutrientsSubstratumTable.addCell(cellNutrientsSubstratumHeader);

                // Create second nutrients substratum table
                Table secondNutrientsSubstratumTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();

                // Create second nutrients substratum table header
                String[] secondNutrientsSubstratumTableHeader = {
                                context.getResources().getString(R.string.headerBlock),
                                context.getResources().getString(R.string.headerNitrateSymbol),
                                context.getResources().getString(R.string.headerCalciumSymbol),
                                context.getResources().getString(R.string.headerSodiumSymbol),
                                context.getResources().getString(R.string.headerPotassiumSymbol),
                                context.getResources().getString(R.string.headerPhSymbol),
                                context.getResources().getString(R.string.headerConductivitySymbol) };
                for (String header : secondNutrientsSubstratumTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondNutrientsSubstratumTable.addCell(cell);
                }

                // Create third nutrients substratum table
                Table thirdNutrientsSubstratumTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();

                // Get boolean to know if nutrients substratum is empty
                boolean isNutrientsSubstratumEmpty = true;
                for (Nutrient nutrient : nutrientList) {
                        if (isNutrientsSubstratumEmpty) {
                                if (nutrient.getNitrate().get(2) != 0)
                                        isNutrientsSubstratumEmpty = false;
                                if (nutrient.getCalcium().get(2) != 0)
                                        isNutrientsSubstratumEmpty = false;
                                if (nutrient.getSodium().get(2) != 0)
                                        isNutrientsSubstratumEmpty = false;
                                if (nutrient.getPotassium().get(2) != 0)
                                        isNutrientsSubstratumEmpty = false;
                                if (nutrient.getPh().get(2) != 0.0)
                                        isNutrientsSubstratumEmpty = false;
                                if (nutrient.getConductivity().get(2) != 0.0)
                                        isNutrientsSubstratumEmpty = false;
                        } else
                                break;
                }

                if (!isNutrientsSubstratumEmpty) {
                        // Create third nutrients substratum table info
                        position = 0;
                        for (Nutrient nutrient : nutrientList) {
                                // Add block name
                                Cell nameCell = new Cell()
                                                .add(new Paragraph(nutrientsBlocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsSubstratumTable.addCell(nameCell);

                                // Add nitrate info
                                Cell nitrateCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getNitrate().get(2))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getNitrate().get(2), 0));
                                thirdNutrientsSubstratumTable.addCell(nitrateCell);

                                // Add calcium info
                                Cell calciumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getCalcium().get(2))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getCalcium().get(2), 0));
                                thirdNutrientsSubstratumTable.addCell(calciumCell);

                                // Add sodium info
                                Cell sodiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getSodium().get(2))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getSodium().get(2), 1));
                                thirdNutrientsSubstratumTable.addCell(sodiumCell);

                                // Add potassium info
                                Cell potassiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPotassium().get(2))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getPotassium().get(2), 0));
                                thirdNutrientsSubstratumTable.addCell(potassiumCell);

                                // Add ph info
                                Cell phCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPh().get(2))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(Formatting.getColorForNutrient(context,
                                                                unit.getCrop(), nutrient.getPh().get(2), 2));
                                thirdNutrientsSubstratumTable.addCell(phCell);

                                // Add conductivity info
                                Cell conductivityCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getConductivity().get(2))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsSubstratumTable.addCell(conductivityCell);

                                // Increment position
                                position++;
                        }
                }

                // Create nutrients (foliage) table
                Table nutrientsFoliageTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create nutrients (foliage) table header
                String nutrientsFoliageHeader = context.getResources().getString(R.string.pdf_header_nutrients_foliage);
                Cell cellNutrientsFoliageHeader = new Cell().add(new Paragraph(nutrientsFoliageHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                nutrientsFoliageTable.addCell(cellNutrientsFoliageHeader);

                // Create second nutrients foliage table
                Table secondNutrientsFoliageTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();

                // Create second nutrients foliage table header
                String[] secondNutrientsFoliageTableHeader = { context.getResources().getString(R.string.headerBlock),
                                context.getResources().getString(R.string.headerNitrateSymbol),
                                context.getResources().getString(R.string.headerCalciumSymbol),
                                context.getResources().getString(R.string.headerSodiumSymbol),
                                context.getResources().getString(R.string.headerPotassiumSymbol),
                                context.getResources().getString(R.string.headerPhSymbol),
                                context.getResources().getString(R.string.headerConductivitySymbol) };
                for (String header : secondNutrientsFoliageTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondNutrientsFoliageTable.addCell(cell);
                }

                // Create third nutrients foliage table
                Table thirdNutrientsFoliageTable = new Table(UnitValue
                                .createPercentArray(new float[] { 0.15f, 0.14f, 0.14f, 0.14f, 0.14f, 0.14f, 0.15f }))
                                .useAllAvailableWidth();

                // Get boolean to know if nutrients foliage is empty
                boolean isNutrientsFoliageEmpty = true;
                for (Nutrient nutrient : nutrientList) {
                        if (isNutrientsFoliageEmpty) {
                                if (nutrient.getNitrate().get(3) != 0)
                                        isNutrientsFoliageEmpty = false;
                                if (nutrient.getCalcium().get(3) != 0)
                                        isNutrientsFoliageEmpty = false;
                                if (nutrient.getSodium().get(3) != 0)
                                        isNutrientsFoliageEmpty = false;
                                if (nutrient.getPotassium().get(3) != 0)
                                        isNutrientsFoliageEmpty = false;
                                if (nutrient.getPh().get(3) != 0.0)
                                        isNutrientsFoliageEmpty = false;
                                if (nutrient.getConductivity().get(3) != 0.0)
                                        isNutrientsFoliageEmpty = false;
                        } else
                                break;
                }

                if (!isNutrientsFoliageEmpty) {
                        // Create third nutrients foliage table info
                        position = 0;
                        for (Nutrient nutrient : nutrientList) {
                                // Add block name
                                Cell nameCell = new Cell()
                                                .add(new Paragraph(nutrientsBlocks.get(position).getName()))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsFoliageTable.addCell(nameCell);

                                // Add nitrate info
                                Cell nitrateCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getNitrate().get(3))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsFoliageTable.addCell(nitrateCell);

                                // Add calcium info
                                Cell calciumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getCalcium().get(3))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsFoliageTable.addCell(calciumCell);

                                // Add sodium info
                                Cell sodiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getSodium().get(3))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsFoliageTable.addCell(sodiumCell);

                                // Add potassium info
                                Cell potassiumCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPotassium().get(3))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsFoliageTable.addCell(potassiumCell);

                                // Add ph info
                                Cell phCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getPh().get(3))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsFoliageTable.addCell(phCell);

                                // Add conductivity info
                                Cell conductivityCell = new Cell()
                                                .add(new Paragraph(String.valueOf(nutrient.getConductivity().get(3))))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                                thirdNutrientsFoliageTable.addCell(conductivityCell);

                                // Increment position
                                position++;
                        }
                }

                // Create empty nutrients table
                Table emptyNutrientsTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create nutrients (foliage) table header
                String emptyNutrientsHeader = context.getResources().getString(R.string.pdf_header_empty_nutrients);
                Cell cellEmptyNutrientsHeader = new Cell().add(new Paragraph(emptyNutrientsHeader))
                                .setTextAlignment(TextAlignment.CENTER);
                emptyNutrientsTable.addCell(cellEmptyNutrientsHeader);

                // Create observations table
                Table observationsTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create observations table header
                String observationsHeader = context.getResources().getString(R.string.pdf_header_observations);
                Cell cellObservationsHeader = new Cell().add(new Paragraph(observationsHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                observationsTable.addCell(cellObservationsHeader);

                // Create second observations table
                Table secondObservationsTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create second observations table header
                String observations = diagnostic.getObservations();
                if (observations.isEmpty())
                        observations = context.getResources().getString(R.string.no_observations);
                Cell cellSecondObservationsHeader = new Cell().add(new Paragraph(observations))
                                .setTextAlignment(TextAlignment.JUSTIFIED);
                secondObservationsTable.addCell(cellSecondObservationsHeader);

                // Create score table
                Table scoreTable = new Table(UnitValue.createPercentArray(new float[] { 1 })).useAllAvailableWidth();

                // Create score table header
                String scoreHeader = context.getResources().getString(R.string.pdf_header_score);
                Cell cellScoreHeader = new Cell().add(new Paragraph(scoreHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                scoreTable.addCell(cellScoreHeader);

                // Create second score table
                Table secondScoreTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.20f, 0.20f, 0.20f, 0.20f, 0.20f }))
                                .useAllAvailableWidth();

                // Create second score table header
                String[] secondScoreTableHeader = { context.getResources().getString(R.string.headerDevelopment),
                                context.getResources().getString(R.string.headerSanity),
                                context.getResources().getString(R.string.headerManagement),
                                context.getResources().getString(R.string.headerTotal),
                                context.getResources().getString(R.string.headerAverage) };
                for (String header : secondScoreTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondScoreTable.addCell(cell);
                }

                // Create third score table
                Table thirdScoreTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.20f, 0.20f, 0.20f, 0.20f, 0.20f }))
                                .useAllAvailableWidth();

                // Fill third score table
                Cell developmentCell = new Cell()
                                .add(new Paragraph(String.valueOf(diagnostic.getDevelopment())))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdScoreTable.addCell(developmentCell);

                Cell sanityCell = new Cell()
                                .add(new Paragraph(String.valueOf(diagnostic.getSanity())))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdScoreTable.addCell(sanityCell);

                Cell managementCell = new Cell()
                                .add(new Paragraph(String.valueOf(diagnostic.getManagement())))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdScoreTable.addCell(managementCell);

                int total = diagnostic.getDevelopment() + diagnostic.getSanity() + diagnostic.getManagement();
                Cell totalCell = new Cell()
                                .add(new Paragraph(String.valueOf(total)))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdScoreTable.addCell(totalCell);

                Cell averageCell = new Cell()
                                .add(new Paragraph(String.format(Locale.getDefault(), "%.1f", (total / 3.0))))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdScoreTable.addCell(averageCell);

                // Create execution table
                Table executionTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .useAllAvailableWidth();

                // Create execution table header
                String executionHeader = context.getResources().getString(R.string.pdf_header_execution);
                Cell cellExecutionHeader = new Cell().add(new Paragraph(executionHeader))
                                .setBackgroundColor(new DeviceRgb(47, 113, 43))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 255, 255))
                                .setTextAlignment(TextAlignment.CENTER);
                executionTable.addCell(cellExecutionHeader);

                // Create second execution table
                Table secondExecutionTable = new Table(
                                UnitValue.createPercentArray(new float[] { 0.25f, 0.25f, 0.50f }))
                                .useAllAvailableWidth();

                // Create second execution table header
                String[] secondExecutionTableHeader = { context.getResources().getString(R.string.header_applications),
                                context.getResources().getString(R.string.header_fertilizers),
                                context.getResources().getString(R.string.header_tendency) };
                for (String header : secondExecutionTableHeader) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                        secondExecutionTable.addCell(cell);
                }

                // Create third execution table
                Table thirdExecutionTable = new Table(UnitValue.createPercentArray(new float[] { 0.25f, 0.25f, 0.50f }))
                                .useAllAvailableWidth();

                // Fill third execution table
                Cell applicationCell = new Cell()
                                .add(new Paragraph(diagnostic.getExecutionApplications() + "%"))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdExecutionTable.addCell(applicationCell);

                Cell fertilizerCell = new Cell()
                                .add(new Paragraph(diagnostic.getExecutionFertilizers() + "%"))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdExecutionTable.addCell(fertilizerCell);

                Cell tendencyCell = new Cell()
                                .add(new Paragraph(diagnostic.getTendency()))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                thirdExecutionTable.addCell(tendencyCell);

                // Add table to the document
                document.add(consultantParagraph);
                document.add(tableGeneralInfo);
                document.add(secondTableInfo);
                document.add(thirdTableInfo);
                document.add(phenologyTable);
                document.add(secondPhenologyTable);
                document.add(thirdPhenologyTable);
                document.add(chartImage);
                document.add(plagueTable);
                document.add(secondPlagueTable);
                document.add(thirdPlagueTable);
                document.add(deficiencyTable);
                document.add(secondDeficiencyTable);
                document.add(thirdDeficiencyTable);
                document.add(activityTable);
                document.add(secondActivityTable);
                document.add(thirdActivityTable);
                document.add(taskTable);
                document.add(secondTaskTable);
                document.add(thirdTaskTable);
                if (isHarvestInfo) {
                        document.add(harvestTable);
                        document.add(secondHarvestTable);
                        document.add(thirdHarvestTable);
                }
                document.add(nutrientsDropperTable);
                if (!isNutrientsDropperEmpty) {
                        document.add(secondNutrientsDropperTable);
                        document.add(thirdNutrientsDropperTable);
                } else
                        document.add(emptyNutrientsTable);
                document.add(nutrientsDrainageTable);
                if (!isNutrientsDrainageEmpty) {
                        document.add(secondNutrientsDrainageTable);
                        document.add(thirdNutrientsDrainageTable);
                } else
                        document.add(emptyNutrientsTable);
                document.add(nutrientsSubstratumTable);
                if (!isNutrientsSubstratumEmpty) {
                        document.add(secondNutrientsSubstratumTable);
                        document.add(thirdNutrientsSubstratumTable);
                } else
                        document.add(emptyNutrientsTable);
                document.add(nutrientsFoliageTable);
                if (!isNutrientsFoliageEmpty) {
                        document.add(secondNutrientsFoliageTable);
                        document.add(thirdNutrientsFoliageTable);
                } else
                        document.add(emptyNutrientsTable);
                document.add(observationsTable);
                document.add(secondObservationsTable);
                document.add(scoreTable);
                document.add(secondScoreTable);
                document.add(thirdScoreTable);
                document.add(executionTable);
                document.add(secondExecutionTable);
                document.add(thirdExecutionTable);

                // Get face image
                double average = total / 3.0;
                Drawable faceDrawable;
                if (average >= 8)
                        faceDrawable = ContextCompat.getDrawable(context, R.drawable.ic_happy_face);
                else if (average >= 6)
                        faceDrawable = ContextCompat.getDrawable(context, R.drawable.ic_serious_face);
                else
                        faceDrawable = ContextCompat.getDrawable(context, R.drawable.ic_sad_face);

                // Add face image to the document
                if (faceDrawable != null) {
                        Bitmap faceBitmap = getBitmapFromDrawable(faceDrawable);
                        ByteArrayOutputStream faceStream = new ByteArrayOutputStream();
                        faceBitmap.compress(Bitmap.CompressFormat.PNG, 100, faceStream);
                        byte[] faceStreamByteArray = faceStream.toByteArray();

                        try {
                                ImageData data = ImageDataFactory.create(faceStreamByteArray);
                                Image img = new Image(data);

                                // Adjust size of the image
                                float desiredWidth = 150;
                                float aspectRatio = img.getImageWidth() / img.getImageHeight();
                                img.scaleAbsolute(desiredWidth, desiredWidth / aspectRatio);

                                // Top margin
                                float marginTop = 20;
                                img.setMarginTop(marginTop);

                                // Center horizontal
                                img.setHorizontalAlignment(HorizontalAlignment.CENTER);

                                document.add(img);
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }

                // Close document
                document.close();
        }

        private static Bitmap createBarChartBitmap(Context context, List<Block> blocks, List<Item> items,
                        boolean avocado) {
                // Create canvas with white background
                Bitmap bitmap = Bitmap.createBitmap(500, 300, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);

                // Get filtered items
                String donePhenology = context.getResources().getString(R.string.done);
                List<Item> filteredItems = new ArrayList<>();
                List<Block> filteredBlocks = new ArrayList<>();

                for (int i = 0; i < items.size(); i++) {
                        if (!items.get(i).getPhenology().equals(donePhenology)) {
                                filteredItems.add(items.get(i));
                                filteredBlocks.add(blocks.get(i));
                        }
                }

                // Add margins
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                int marginLeft = 40;
                int marginTop = 40;
                int maxBarHeight = 200;
                float barWidth = (float) 500 / filteredItems.size();

                // Draw title
                paint.setColor(Color.BLACK);
                paint.setTextSize(28);
                paint.setTextAlign(Paint.Align.LEFT);

                // Adjust position
                float textX = 0;
                float textY = marginTop + (maxBarHeight / 2f);

                // Update canvas
                canvas.save();
                canvas.rotate(-90, textX, textY);
                canvas.drawText(context.getResources().getString(R.string.pdf_header_heights),
                                textX - (maxBarHeight / 2f), textY + 20, paint);
                canvas.restore();

                // Draw the box for the bar chart
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                float boxBottom = marginTop + maxBarHeight;
                canvas.drawRect(marginLeft, (float) marginTop, 490, boxBottom, paint);

                // Draw horizontal lines every 20 cm with reduced alpha (transparency)
                for (int i = 20; i <= maxBarHeight; i += 20) {
                        float y = boxBottom - (float) i;
                        paint.setColor(Color.BLACK);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setAlpha(64);
                        canvas.drawLine(marginLeft, y, 490, y, paint);
                        paint.setAlpha(255);
                }

                for (int position = 0; position < filteredItems.size(); position++) {
                        // Get block and item
                        Block block = filteredBlocks.get(position);
                        Item item = filteredItems.get(position);
                        float divisor = avocado ? 600 : 200;
                        float barHeight = (float) item.getHeight() / divisor * maxBarHeight;
                        int myGreenColor = ContextCompat.getColor(context, R.color.primary);
                        paint.setColor(myGreenColor);

                        // Draw each filled bar inside the box
                        float left = position * barWidth + marginLeft + 10;
                        float right = (position + 1) * barWidth - 10;
                        float top = boxBottom - barHeight;

                        // Adjust the filling even if the height is pretty low
                        if (top == boxBottom) {
                                top -= 1;
                        }

                        // Draw the bar
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawRect(left, top, right, boxBottom, paint);

                        // Calculate the central position of each bar for the text
                        float centerX = (left + right) / 2;

                        // Save current canvas and rotate
                        canvas.save();
                        canvas.rotate(-45, centerX, boxBottom + 20);

                        // Draw block name on the bottom of each bar
                        paint.setColor(Color.BLACK);
                        paint.setTextSize(20);
                        paint.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText(block.getName(), centerX, boxBottom + 20, paint);

                        // Restore canvas
                        canvas.restore();

                        // Draw value on top of each bar
                        if (top - 5 > marginTop) {
                                String heightText = String.valueOf(item.getHeight());

                                // Verify if the number is an integer
                                if (heightText.endsWith(".0"))
                                        heightText = heightText.substring(0, heightText.length() - 2);

                                // Draw new text
                                canvas.drawText(heightText, (left + right) / 2, top - 5, paint);
                        }
                }

                return bitmap;
        }

        private static Bitmap getBitmapFromDrawable(Drawable drawable) {
                if (drawable instanceof BitmapDrawable) {
                        return ((BitmapDrawable) drawable).getBitmap();
                } else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
                        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                        drawable.getIntrinsicHeight(),
                                        Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                        return bitmap;
                } else {
                        throw new IllegalArgumentException("Unsupported drawable type");
                }
        }

}
