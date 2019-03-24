package com.edlplan.beatmapservice;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

public class ModeIconComp {


    public static class IconHolder extends RecyclerView.ViewHolder {

        public ImageView icon;

        public View bg;

        public IconHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.difImage);
            bg = itemView.findViewById(R.id.selectBG);
        }

        public void setIcon(int mode, double dif) {

        }

        public int selectIcon(int mode, double dif) {
            return 0;
        }

        public void setSelected(boolean b) {
            if (b) {
                bg.setVisibility(View.VISIBLE);
            } else {
                bg.setVisibility(View.GONE);
            }
        }

    }

}

