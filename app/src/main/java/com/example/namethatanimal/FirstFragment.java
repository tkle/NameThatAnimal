package com.example.namethatanimal;


import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

public class FirstFragment extends Fragment {
    private static final String TAG = "FirstFragment";

    private ImageView backgroundImageView; // background image view
    private Animation fadeAnimation; // animation for background image
    private MediaPlayer playMediaPlayer; // play sound
    private boolean enableAudio = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get audio preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String key = getString(R.string.enable_audio);
        enableAudio = sharedPreferences.getBoolean(key, true);

        playMediaPlayer = MediaPlayer.create(getActivity(), R.raw.play); // load play sound
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        fadeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fade_in_out_animation);
        backgroundImageView = (ImageView) view.findViewById(R.id.backgroundImageView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // play animation (fade in/out) on background image
        backgroundImageView.startAnimation(fadeAnimation);

        // Navigate to play screen when user clicks the play button
        view.findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // play audio
                if (enableAudio)
                    playMediaPlayer.start();

                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    // listener for changes to the app's SharedPreferences
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                // called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    Log.d(TAG, "onSharedPreferenceChanged():key=" + key);
                    // Check if user changed audio option
                    if (key.equals(getString(R.string.enable_audio))) {
                        boolean bool = sharedPreferences.getBoolean(key, true);
                        Log.d(TAG, "onSharedPreferenceChanged():bool=" + bool);
                        if (bool) {
                            enableAudio = true;
                        } else {
                            enableAudio = false;
                        }
                    }
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        // release media player resource
        if (playMediaPlayer != null) {
            playMediaPlayer.release();
            playMediaPlayer = null;
        }
    }

}