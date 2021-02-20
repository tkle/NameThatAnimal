package com.example.namethatanimal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.example.namethatanimal.SecondFragment.NUM_OF_QUESTION_IN_GAME;

/*
Class to display dialog with number of guesses and percentage.
If we initialized the dialog with anonymous class, following exception is thrown:

java.lang.IllegalStateException: Fragment null must be a public static class to be properly recreated from instance state.

 */
public class QuizResultDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final SecondFragment quizFragment = getQuizFragment();
        int totalGuesses = quizFragment.getTotalGuesses();
        builder.setMessage(getString(R.string.results,
                totalGuesses,
                100.0 * ((double)NUM_OF_QUESTION_IN_GAME / (double)totalGuesses)));
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        quizFragment.gotoFirstFragment();
                    }
                }
        );
        return builder.create();
    }

    private SecondFragment getQuizFragment() {
        return (SecondFragment) getParentFragmentManager().findFragmentById(R.id.quizFragment);
    }
}
