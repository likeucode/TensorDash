package com.example.tensordash.service.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.tensordash.service.model.Project;
import com.example.tensordash.service.model.ProjectParams;
import com.example.tensordash.service.model.StatusCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FirebaseDatabaseRepository {

    private static final String TAG = "FirebaseDatabaseReposit";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private List<Project> projectList;
    private MutableLiveData<List<Project>> projectMutableLiveData;


    public FirebaseDatabaseRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        projectList = new ArrayList<>();
        projectMutableLiveData = new MutableLiveData<>();
        getProjects();
    }

    private void getProjects() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot projectDataSnapshot, @Nullable String s) {
                Iterator<DataSnapshot> epochLevelIterator = projectDataSnapshot.getChildren().iterator();
                String projectName = projectDataSnapshot.getKey();
                StatusCode status = StatusCode.DEFAULT;
                List<ProjectParams> projectParamsList = new ArrayList<>();
                while (epochLevelIterator.hasNext()) {
                    DataSnapshot epochDataSnapShot = epochLevelIterator.next();
                    if(!epochDataSnapShot.hasChildren()){
                        if(epochDataSnapShot.getKey().equals("Status")){
                            status = StatusCode.valueOf(epochDataSnapShot.getValue().toString());
                        }
                        continue;
                    }
                    int epoch = Integer.parseInt(epochDataSnapShot.child("Epoch").getValue().toString());
                    double accuracy = Double.parseDouble(epochDataSnapShot.child("Accuracy").getValue().toString());
                    double loss = Double.parseDouble(epochDataSnapShot.child("Loss").getValue().toString());
                    double validationLoss = Double.parseDouble(epochDataSnapShot.child("Validation Loss").getValue().toString());
                    double validationAccuracy = Double.parseDouble(epochDataSnapShot.child("Validation_accuracy").getValue().toString());
                    projectParamsList.add(new ProjectParams(epoch, accuracy, loss, validationLoss, validationAccuracy));
                }
                projectList.add(new Project(projectName, status, projectParamsList));
                projectMutableLiveData.setValue(projectList);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public MutableLiveData<List<Project>> getAllProjects(){
        return projectMutableLiveData;
    }


}
