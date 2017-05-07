package com.example.dan_p.hw2.minesweeper;

import android.content.Context;
import android.view.View;
import android.widget.Button;

public class CellButton extends Button implements View.OnClickListener {

    private int row;
    private int column;
    private CellButtonListener listener;

    private boolean isMine;

    public CellButton(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public void setPosition(int row, int column){
        this.row = row;
        this.column = column;
    }

    @Override
    public void onClick(View v) {
        if (listener != null)
        listener.buttonClick(this);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setListener(CellButtonListener listener) {
        this.listener = listener;
    }


    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }
}
