package com.example.dan_p.hw2.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.dan_p.hw2.R;
import com.example.dan_p.hw2.database.PlayerRecord;

import java.util.ArrayList;

public class TableFragment extends Fragment {
    private String difficulty;
    private ArrayList<PlayerRecord> playerRecordArrayList;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_table,container, false);
        this.difficulty = getArguments().getString("difficulty");
        this.playerRecordArrayList =  getArguments().getParcelableArrayList("records");
        return view;
    }

    public static TableFragment newInstance(ArrayList<PlayerRecord> playerRecords, String difficuly) {
        TableFragment myFragment = new TableFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("records", playerRecords);
        args.putString("difficulty", difficuly);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setItems();
    }

    public void setItems() {
        TextView textViewDifficulty = (TextView) view.findViewById(R.id.TEXTVIEW_TABLE_DIFFICULTY);
        textViewDifficulty.setText(this.difficulty);
        TableLayout table = (TableLayout) view.findViewById(R.id.RECORDS_TABLE_FRAGMENT);
        for(int i = 0; i < playerRecordArrayList.size(); i++) {
            View view = table.getChildAt(i+1);
            if (view instanceof TableRow) {
                // then, you can remove the the row you want...
                // for instance...
                TableRow row = (TableRow) view;

                TextView textViewName = (TextView)row.getChildAt(1);
                textViewName.setText(playerRecordArrayList.get(i).getName());

                TextView textViewTime = (TextView)row.getChildAt(2);
                textViewTime.setText(String.format("%d", playerRecordArrayList.get(i).getTime()));
            }

            if (i == 9) break;
        }
    }
}