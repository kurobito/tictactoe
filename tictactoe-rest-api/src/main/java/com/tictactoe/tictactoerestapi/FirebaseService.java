package com.tictactoe.tictactoerestapi;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class FirebaseService {
    public static Firestore db = FirestoreClient.getFirestore();
}
