package com.example.firebasedata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private ListView listViewData;
    private List<Map<String, String>> dataList;
    private SimpleAdapter adapter;
    private FirebaseDatabase fbControl;
    private String TAG="main";
    private DatabaseReference classDB;
    private CursorTreeAdapter snapshot;
    private int dataCount;

    private EditText editName,editPhone;

    private void setView(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setView();

        context=this;
        listViewData = (ListView)findViewById(R.id.listView);

        dataList = new ArrayList<Map<String,String>>();
        dataList.clear();

        /*
        * context：在哪個Activity顯示
        * dataList：放到item Layout的資料
        * item_Layout：每一列要放的樣板
        * 資料 ArrayList的 Key
        * 資料 ArrayList的 value
        * */
        adapter = new SimpleAdapter(context,dataList,R.layout.item_layout,new String[]{"name","phone"},
                                    new int[]{R.id.textView_name,R.id.textView_phone});
        listViewData.setAdapter(adapter);

        //取得FirebaseDatabase物件
        fbControl = FirebaseDatabase.getInstance();
        Log.d(TAG, "onCreate: fbControl="+fbControl);
        //參考資料到class資料夾
        classDB = fbControl.getReference("class");
        Log.d(TAG, "onCreate: classDB = "+classDB);

//        classDB.child("4").child("name").setValue("Richard");
//        classDB.child("4").child("phone").setValue("097777777");

        //TODO:監聽 線上的資料庫 資料更新時 就讓App也更新
        classDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear(); //清空List 等下裝新資料
                dataCount = (int) snapshot.getChildrenCount();//有幾筆資料
                Log.d(TAG, " dataCount="+dataCount);

                for(DataSnapshot ds: snapshot.getChildren()){ //從0~n把snapshot的資料讀完

                    Map<String,String> mapData = new HashMap<String,String>();//建立Map儲存多筆資料的Key和Value(一位使用者的多個屬性資料)
                    //取得snapshot KEY="name"的資料 存到Map裡面
                    String nameValue = (String)ds.child("name").getValue();
                    if(nameValue == null){
                        mapData.put("name","no name");
                    }else {
                        mapData.put("name",nameValue);
                    }
                    //取得snapshot KEY="phone"的資料 存到Map裡面
                    String phoneValue = (String)ds.child("phone").getValue();
                    if(nameValue == null){
                        mapData.put("phone","no phone");
                    }else {
                        mapData.put("phone",phoneValue);
                    }
                    dataList.add(mapData);//更新資料到List 所以一次for迴圈 增加一個人的name和phone
                }//for end

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //TODO:點選Item 跳出dialog視窗確認是否刪除資料
        listViewData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> item = (Map<String, String>) parent.getItemAtPosition(position);
                final String name = item.get("name").toString();
                final String phone = item.get("phone").toString();
                Log.d(TAG, "name = "+name);
                Log.d(TAG, "phone = "+phone);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete data");
                builder.setIcon(R.drawable.delete_icon);

               //製造確認按鈕
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //按下確認按鈕
                        final Query nameData = classDB.orderByChild("name").equalTo(name); //抓 item上的名字 看有哪些firebase上的名字是一樣的
                        //當名字事件有變動 就執行
                        nameData.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //把所有firebase上的資料run過一輪
                                for(DataSnapshot nameSnapShot : snapshot.getChildren()){
                                    //取得firebase上就這個名字的全部人的電話
                                    String phoneString = (String) nameSnapShot.child("phone").getValue();
                                    Log.d(TAG, "check phone from firebase"+phoneString);
                                    //如果連電話都相同 就刪掉
                                    if(phoneString.equals(phone)){
                                        nameSnapShot.getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });


    }
    //TODO:實作 Menu方法
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.add_item:

                addDialog();
                break;
            case R.id.Login_item:
                Intent intent = new Intent(context,LoginActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //TODO: dialog 方法
    public void addDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View layoutView =inflater.inflate(R.layout.dialog,(ViewGroup) findViewById(R.id.dialog_id));
        editName=(EditText)layoutView.findViewById(R.id.editText_addname);
        editPhone=(EditText)layoutView.findViewById(R.id.editText_addphone);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add data");
        builder.setIcon(R.drawable.add_icon);
        builder.setView(layoutView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editName.getText().toString();
                String phone = editPhone.getText().toString();
                Log.d(TAG, "name = "+name);
                Log.d(TAG, "phone = "+phone);

                Map<String ,Object> data = new HashMap<>();
                data.put("name",name);
                data.put("phone",phone);

                //return an AuthResult object when it succeeds:
                //Task<Void> result = classDB.child("6").updateChildren(data);
                Task<Void> result = classDB.child("").push().setValue(data);

                result.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: add ok");
                    }
                });

                result.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: add fail");
                    }
                });

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}