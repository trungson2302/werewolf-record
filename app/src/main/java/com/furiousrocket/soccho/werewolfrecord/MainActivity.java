package com.furiousrocket.soccho.werewolfrecord;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
  private ArrayList<Season_Person> mData;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    database = FirebaseDatabase.getInstance();
    myRef = database.getReference();
    mData=new ArrayList<>();

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
        if(mButtonCapnhat.getText().equals("Cập Nhật")){
          mButtonCapnhat.setText("Cancel");
          mButtonOK.setVisibility(View.VISIBLE);
        }else{
          mButtonCapnhat.setText("Cập Nhật");
          mButtonOK.setVisibility(View.GONE);
        }
      }
    });
    mButtonThem.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        showDialog();
      }
    });
  }
  public void showDialog(){
  MyDialog dialog =new MyDialog();
    dialog.show(getSupportFragmentManager(),"tag");
  }
  public static class MyDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the Builder class for convenient MyDialog construction
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      LayoutInflater inflater = getActivity().getLayoutInflater();
      View view = inflater.inflate(R.layout.dialog, null);
    final EditText editText=view.findViewById(R.id.editText);

      // Inflate and set the layout for the MyDialog
      // Pass null as the parent view because its going in the MyDialog layout
      builder.setView(view)
          // Add action buttons
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              String key = myRef.child(REF).child(mCurrentSs+"").child("record").push().getKey();
              Season_Person person=new Season_Person(mCurrentID,editText.getText().toString());
              myRef.child(REF_CURRENT_ID).setValue(mCurrentID++);
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

  ValueEventListener eventListener=new ValueEventListener() {
    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      mCurrentID=Integer.parseInt(dataSnapshot.child(REF_CURRENT_ID).getValue().toString());
      mCurrentSs=Integer.parseInt(dataSnapshot.child(REF_CURRENT_SS).getValue().toString());
      mData.clear();
      for(DataSnapshot data:dataSnapshot.child(REF).child(mCurrentSs+"").child("record").getChildren()){
        Season_Person person=data.getValue(Season_Person.class);
        mData.add(person);
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
      holder.mStt.setText(position+"");
      holder.mSum.setText(data.get(position).getSum()+"");
      holder.mWolf.setText(data.get(position).getWolf()+"");
      holder.mWin.setText(data.get(position).getWin()+"");
      holder.mVillager.setText(data.get(position).getVillager()+"");
    }

    @Override public int getItemCount() {
      return data.size();
    }
  }
}
