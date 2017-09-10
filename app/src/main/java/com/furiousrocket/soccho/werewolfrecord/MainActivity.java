package com.furiousrocket.soccho.werewolfrecord;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  public static final String REF="season-records";
  public static final String REF_CURRENT_ID="current_id";
  public static final String REF_CURRENT_SS="current_season";
  private static int mCurrentSs;
  private static int mCurrentID;
  @BindView(R.id.my_recycler_view) RecyclerView mRecyclerView;
  @BindView(R.id.button2)Button mButtonCapnhat;
  @BindView(R.id.button3)Button mButtonOK;
  @BindView(R.id.button4)Button mButtonThem;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private FirebaseDatabase database;
  private static DatabaseReference myRef;
  private static ArrayList<Season_Person> mData;
  private static ArrayList<String> mListName;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    database = FirebaseDatabase.getInstance();
    myRef = database.getReference();
    mData=new ArrayList<>();
    mListName=new ArrayList<>();

    //myRef.addListenerForSingleValueEvent(singleValueListener);
    myRef.addValueEventListener(eventListener);

    mRecyclerView.setHasFixedSize(true);
    // use a linear layout manager
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);
    // specify an adapter
    mAdapter = new MyAdaper(this,mData);
    mRecyclerView.setAdapter(mAdapter);
    mButtonCapnhat.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        CapNhatDialog dialog=new CapNhatDialog();
        dialog.show(getSupportFragmentManager(),"tag");
        //if(mButtonCapnhat.getText().equals("Cập Nhật")){
        //  mButtonCapnhat.setText("Cancel");
        //  mButtonOK.setVisibility(View.VISIBLE);
        //}else{
        //  mButtonCapnhat.setText("Cập Nhật");
        //  mButtonOK.setVisibility(View.GONE);
        //}
      }
    });

    mButtonThem.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        showDialog();
      }
    });
  }
  public void showDialog(){
  ThemMoiDialog dialog =new ThemMoiDialog();
    dialog.show(getSupportFragmentManager(),"tag");
  }
  public static class CapNhatDialog extends DialogFragment{
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
      getDialog().setCanceledOnTouchOutside(false);
      return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
      final ArrayList<String> mSelectedItems = new ArrayList<>();
      final String[] list=new String[mData.size()];
      for (int i=0;i<mData.size();i++){
        list[i]=mData.get(i).getName();
      }
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      // Set the dialog title
      builder.setTitle("Cập Nhật")
          // Specify the list array, the items to be selected by default (null for none),
          // and the listener through which to receive callbacks when items are selected
          .setMultiChoiceItems(list,null,
              new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which,
                    boolean isChecked) {
                  if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    mSelectedItems.add(mListName.get(which).toString());
                    Toast.makeText(getContext(), mSelectedItems.toString(), Toast.LENGTH_SHORT).show();

                  } else if (mSelectedItems.contains(mListName.get(which).toString())) {
                    // Else, if the item is already in the array, remove it
                    mSelectedItems.remove(mListName.get(which).toString());
                    Toast.makeText(getContext(), mSelectedItems.toString(), Toast.LENGTH_SHORT).show();
                  }
                }
              })
          .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {

            }
          })
          // Set the action buttons
          .setPositiveButton("Wolf", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              // User clicked OK, so save the mSelectedItems results somewhere
              // or return them to the component that opened the dialog
              upDate("wolf",mData,mSelectedItems);
            }
          })
          .setNegativeButton("Villager", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              upDate("villager",mData,mSelectedItems);
            }
          });
      return builder.create();
    }
  }
  public static class ThemMoiDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the Builder class for convenient ThemMoiDialog construction
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      LayoutInflater inflater = getActivity().getLayoutInflater();
      View view = inflater.inflate(R.layout.dialog, null);
    final EditText editText=view.findViewById(R.id.editText);

      // Inflate and set the layout for the ThemMoiDialog
      // Pass null as the parent view because its going in the ThemMoiDialog layout
      builder.setView(view)
          // Add action buttons
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              String key = myRef.child(REF).child(mCurrentSs+"").child("record").push().getKey();
              Season_Person person=new Season_Person(mCurrentID+"",editText.getText().toString());
              myRef.child(REF_CURRENT_ID).setValue(mCurrentID+1);
              Map<String, Object> postValues = person.toMap();

              Map<String, Object> childUpdates = new HashMap<>();
              childUpdates.put( "/"+REF+"/"+mCurrentSs+"/record/"+key, postValues);
              myRef.updateChildren(childUpdates);
            }
          })
          .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dismiss();
            }
          });
      return builder.create();
    }
  }

  public static void upDate(String s,ArrayList<Season_Person> data,ArrayList list){
    for (int i=0;i<data.size();i++){
      data.get(i).setSum(data.get(i).getSum()+1);
    }
    switch (s){
      case "wolf":
        for(int j=0;j<list.size();j++){
          for (int x=0;x<data.size();x++){
            if(list.get(j).equals(data.get(x).getName())){
              data.get(x).setWolf(data.get(x).getWolf()+1);
              data.get(x).setWin(data.get(x).getWin()+1);
            }
          }
        }
        break;
      case "villager":
        for(int j=0;j<list.size();j++){
          for (int x=0;x<data.size();x++){
            if(list.get(j).equals(data.get(x).getName())){
              data.get(x).setVillager(data.get(x).getVillager()+1);
              data.get(x).setWin(data.get(x).getWin()+1);
            }
          }
        }
        break;
    }
    for (int i=0;i<data.size();i++) {
      myRef.child(REF).child(mCurrentSs + "").child("record").child(data.get(i).getId()).setValue(data.get(i));
    }
  }
  ValueEventListener eventListener=new ValueEventListener() {
    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      mCurrentID=Integer.parseInt(dataSnapshot.child(REF_CURRENT_ID).getValue().toString());
      mCurrentSs=Integer.parseInt(dataSnapshot.child(REF_CURRENT_SS).getValue().toString());
      mData.clear();
      mListName.clear();
      for(DataSnapshot data:dataSnapshot.child(REF).child(mCurrentSs+"").child("record").getChildren()){
        Season_Person person=data.getValue(Season_Person.class);
        person.setId(data.getKey());
        mData.add(person);
        mListName.add(person.getName());
        mAdapter.notifyDataSetChanged();
      }

    }
    @Override public void onCancelled(DatabaseError databaseError) {
    }
  };
  ValueEventListener singleValueListener =new ValueEventListener() {
    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      mCurrentID=Integer.parseInt(dataSnapshot.child(REF_CURRENT_ID).getValue().toString());
      mCurrentSs=Integer.parseInt(dataSnapshot.child(REF_CURRENT_SS).getValue().toString());
    }

    @Override public void onCancelled(DatabaseError databaseError) {

    }
  };
  public class MyAdaper extends RecyclerView.Adapter<MyAdaper.ViewHolder>{

    private ArrayList<Season_Person> data;
    private Context mContext;

    public MyAdaper(Context context,ArrayList<Season_Person> data){
      this.data=data;
      this.mContext=context;

    }
    public class ViewHolder extends RecyclerView.ViewHolder{
      @BindView(R.id.stt)TextView mStt;
      @BindView(R.id.name)TextView mName;
      @BindView(R.id.sum)TextView mSum;
      @BindView(R.id.wolf)TextView mWolf;
      @BindView(R.id.villager)TextView mVillager;
      @BindView(R.id.win)TextView mWin;
      public ViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            Animation fadeout=new AlphaAnimation(1f,0.5f);
            fadeout.setDuration(500);
            view.setAnimation(fadeout);
          }
        });
        ButterKnife.bind(this,itemView);
      }
    }
    @Override public MyAdaper.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View inflatedView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.list_item, parent, false);
      return new ViewHolder(inflatedView);
    }

    @Override public void onBindViewHolder(MyAdaper.ViewHolder holder, int position) {
      holder.mName.setText(data.get(position).getName());
      holder.mStt.setText(position+1+"");
      holder.mSum.setText(data.get(position).getSum()+"");
      holder.mWolf.setText(data.get(position).getWolf()+"");
      holder.mWin.setText(data.get(position).getWin()+"");
      holder.mVillager.setText(data.get(position).getVillager()+"");
      
      //  // TODO: 9/10/2017
    }

    @Override public int getItemCount() {
      return data.size();
    }
  }
}
