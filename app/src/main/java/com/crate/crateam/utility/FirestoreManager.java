package com.crate.crateam.utility;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.firestore.PersistentCacheIndexManager;
import com.crate.crateam.utility.FirestoreManager;
import com.google.firebase.firestore.PersistentCacheSettings;

public class FirestoreManager {

    private static FirebaseFirestore db;
    private FirestoreManager() {
        // Private constructor to prevent direct instantiation
    }

    public static FirebaseFirestore getInstance() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    public static void initPersistentIndexManager(){
        PersistentCacheIndexManager indexManager = FirebaseFirestore.getInstance().getPersistentCacheIndexManager();
        if (indexManager != null) {
            indexManager.enableIndexAutoCreation();
        }
        FirebaseFirestore.getInstance().getPersistentCacheIndexManager().enableIndexAutoCreation();
    }

}
