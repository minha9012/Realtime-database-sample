package com.example.realtimedatabasesample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimedatabasesample.databinding.ActivityMainBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding : ActivityMainBinding
    private lateinit var database : DatabaseReference
    private var drUser: DatabaseReference? = null

    val db = FirebaseDatabase.getInstance()
    val userList = arrayListOf<User>()
    val adapter = UserAdapter(userList)


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.rvUserList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvUserList.adapter = adapter

        database = Firebase.database.reference //DatabaseReference

        /* 유저 읽기 */
        binding.btnReload.setOnClickListener(View.OnClickListener {
            database.child("users").get().addOnSuccessListener {

                // 1. 데이터 1회 읽어 RecyclerView에 담기
                /*
                userList.clear()
                for(userValue in it.children){
                    //jsonObject -> ModelClass by Gson
                    val user = Gson().fromJson(userValue.value.toString(), User::class.java)
                    userList.add(user)
                }
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "유저 읽기 성공", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Got value ${it.value}")
                */
                // 2. 데이터 변화 감지하는 ValueEventListener add
                addUserListener(database)

            }.addOnFailureListener {
                Toast.makeText(this, "유저 읽기 실패", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Error getting data! $it")
            }
        })

        /* 유저 추가 */
        binding.btnWrite.setOnClickListener(View.OnClickListener {
            //동적으로 AlertDialog 생성
            val builder = AlertDialog.Builder(this)
            val tvName = TextView(this)
            val tvAddress = TextView(this)
            val tvAge = TextView(this)
            tvName.text = "Name"
            tvAddress.text = "Address"
            tvAge.text = "Age"

            val etName = EditText(this)
            val etAddress = EditText(this)
            val etAge = EditText(this)
            etAddress.isSingleLine = true

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(16, 16, 16, 16)
            layout.addView(tvName)
            layout.addView(etName)
            layout.addView(tvAge)
            layout.addView(etAge)
            layout.addView(tvAddress)
            layout.addView(etAddress)

            builder.setView(layout)
                .setTitle("유저 추가")
                .setPositiveButton(R.string.add) { _, _ ->
                    val user = hashMapOf(
                        "name" to etName.text.toString(),
                        "address" to etAddress.text.toString(),
                        "age" to etAge.text.toString().toLong().toInt()
                    )
                    database.child("users").push().setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "유저 추가 성공", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "유저 추가 실패", Toast.LENGTH_SHORT).show()
                            Log.w(TAG, "error! $it")
                        }

//                    binding.btnReload.callOnClick() //새로고침 버튼 클릭
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        })

        /* 유저 삭제 */
        binding.btnDelete.setOnClickListener(View.OnClickListener {
            database.child("users").removeValue().addOnSuccessListener {
                Toast.makeText(this, "유저 삭제 완료", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "유저 삭제 실패", Toast.LENGTH_SHORT).show()
            }
            adapter.notifyDataSetChanged()
        })

    }

    fun basicReadWrite() {
        // [START write_message]
        // Write a message to the database
        val database = Firebase.database
        val myRef = database.getReference("message")

        myRef.setValue("Hello, World!")
        // [END write_message]

        // [START read_message]
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                Log.d(TAG, "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        // [END read_message]
    }

    private fun addUserListener(userDataReference: DatabaseReference){
        val userListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.hasChild("users")){
                    userList.clear()

                    for ( userValue in dataSnapshot.child("users").children){
                        val user = Gson().fromJson(userValue.value.toString(), User::class.java)
                        userList.add(user)
                    }

                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUsers:onCancelled", databaseError.toException())
            }
        }
        userDataReference.addValueEventListener(userListener)
    }

}