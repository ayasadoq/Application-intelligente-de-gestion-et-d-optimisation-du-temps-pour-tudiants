package com.studyflowai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.studyflowai.data.model.Student
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SocialViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val _activeStudents = MutableLiveData<List<Student>>()
    val activeStudents: LiveData<List<Student>> = _activeStudents

    private var currentRoomRef: com.google.firebase.database.DatabaseReference? = null
    private var roomListener: ValueEventListener? = null

    fun joinRoom(roomCode: String, myName: String) {
        // Nettoyer l'ancien écouteur si existant
        currentRoomRef?.removeEventListener(roomListener!!)

        val studentId = "user_" + System.currentTimeMillis()
        val me = Student(id = studentId, name = myName, status = "En mission")

        currentRoomRef = database.getReference("rooms").child(roomCode)
        
        // Ajouter ma présence
        currentRoomRef?.child("members")?.child(studentId)?.setValue(me)
        
        // Supprimer ma présence quand je me déconnecte (Optionnel mais recommandé)
        currentRoomRef?.child("members")?.child(studentId)?.onDisconnect()?.removeValue()

        // Écouter les membres de la salle
        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val students = mutableListOf<Student>()
                snapshot.child("members").children.forEach {
                    it.getValue(Student::class.java)?.let { student ->
                        students.add(student)
                    }
                }
                _activeStudents.postValue(students)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        currentRoomRef?.addValueEventListener(roomListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        currentRoomRef?.removeEventListener(roomListener!!)
    }
}

