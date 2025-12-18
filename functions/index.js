const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// Cloud function to create new consultant
exports.createUserAndDocument = functions.https.onCall((data, context) => {
  const email = `${data.username}@agriberries.mx`;
  const password = data.password;

  return admin.auth().createUser({
    email: email,
    password: password,
  })
      .then((userRecord) => {
        const uid = userRecord.uid;
        const consultantData = {
          id: uid,
          username: data.username,
          name: data.name,
          phone: data.phone,
          category: data.category,
          birth: new Date(data.birth),
          blocked: false,
          deleted: null,
        };

        return admin.firestore().collection("consultants").doc(uid)
            .set(consultantData);
      })
      .then(() => {
        return {success: true};
      })
      .catch((error) => {
        return {success: false, error: error.message};
      });
});

// Cloud function to toggle block status from consultant
exports.toggleUserBlockStatus = functions.https.onCall((data, context) => {
  // Basic validation
  if (!data.uid) {
    throw new functions.https.HttpsError("invalid-argument",
        "The function must be called with a UID.");
  }

  if (typeof data.blocked !== "boolean") {
    throw new functions.https.HttpsError("invalid-argument",
        "The function must be called with a valid blocked status.");
  }

  const uid = data.uid;
  const blocked = data.blocked;

  const userRef = admin.firestore().collection("consultants").doc(uid);

  return admin.firestore().runTransaction((transaction) => {
    // Get Firebase Firestore document
    return transaction.get(userRef).then((doc) => {
      if (!doc.exists) {
        throw new functions.https.HttpsError("not-found",
            "The user does not exist in Firestore.");
      }

      // Update blocked status
      transaction.update(userRef, {blocked: blocked});
    });
  })
      .then(() => {
        // Update status from Firebase auth
        return admin.auth().updateUser(uid, {
          disabled: blocked,
        });
      })
      .then(() => {
        return {success: true};
      })
      .catch((error) => {
        return {success: false, error: error.message};
      });
});

// Update consultant password
exports.updateUserPassword = functions.https.onCall((data, context) => {
  const uid = data.uid;
  const newPassword = data.password;

  // Basic verification
  if (!uid || !newPassword) {
    return {success: false, error: "UID and password are required"};
  }

  return admin.auth().updateUser(uid, {
    password: newPassword,
  })
      .then((userRecord) => {
        return {success: true};
      })
      .catch((error) => {
        return {success: false, error: error.message};
      });
});

// Triggers
exports.onConsultantDeleted = functions.firestore
    .document("consultants/{consultantId}")
    .onDelete((snap, context) => {
      // Get consultantId to use it as the UID from Firebase Auth
      const uid = context.params.consultantId;

      // Delete user from Firebase Auth using the UID
      return admin.auth().deleteUser(uid)
          .then(() => {
            console.log(`Successfully deleted auth user with UID: ${uid}`);
            return null;
          })
          .catch((error) => {
            console.error(`Error deleting auth user with UID: ${uid}:`, error);
            throw new functions.https.HttpsError("internal", `Error:
              ${error.message}`);
          });
    });

exports.sendNotificationToAllUsers = functions.firestore
    .document("notifications/{notificationId}")
    .onCreate((snap, context) => {
      const notificationInfo = snap.data();

      // Create message
      const message = {
        notification: {
          title: notificationInfo.title,
          body: notificationInfo.text,
        },
        topic: "allUsers",
      };

      return admin.messaging().send(message)
          .then((response) => {
            // Response correctly
            console.log("Notificación enviada exitosamente:", response);
            return null;
          })
          .catch((error) => {
            // Error
            console.error("Error enviando notificación:", error);
            throw new functions.https.HttpsError("internal",
                `Error enviando notificación: ${error.message}`);
          });
    });
