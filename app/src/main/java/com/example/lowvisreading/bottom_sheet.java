package com.example.lowvisreading;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
public class bottom_sheet extends BottomSheetDialogFragment{


    public interface BottomSheetListener {
        void onTextSizeChanged(int textSize);
        void onBlindSpotSizeChanged(int blindSpotSize);
    }

    private BottomSheetListener mListener;

    // Method to attach the listener
    public void setBottomSheetListener(BottomSheetListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        SeekBar textSizeSeekBar = view.findViewById(R.id.textSize);
        SeekBar blindSpotSizeSeekBar = view.findViewById(R.id.blindSpotSize);

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mListener != null) {
                    mListener.onTextSizeChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // You can also implement this if needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // You can also implement this if needed
            }
        });

        blindSpotSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mListener != null) {
                    mListener.onBlindSpotSizeChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // You can also implement this if needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // You can also implement this if needed
            }
        });

        return view;
    }
}
