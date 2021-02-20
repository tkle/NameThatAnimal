package com.example.namethatanimal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecondFragment extends Fragment {

    private static final String TAG = "SecondFragment";

    public static final int NUM_OF_QUESTION_IN_GAME = 10;

    private List<String> fileNameList; // animal file names
    private List<String> quizAnimalsFileList; // animals in current quiz
    private String correctAnswer; // correct animal for current image
    private int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct answers
    private SecureRandom random; // used to randomize the questions
    private Handler handler; // used to delay loading next animal
    private Animation correctAnimation; // animation for incorrect guess

    private LinearLayout quizLinearLayout; // layout that contains the quiz
    private TextView questionNumberTextView; // shows current question #
    private ImageView animalImageView; // displays the animal image
    private LinearLayout[] guessLinearLayouts; // rows of answer Buttons
    private TextView answerTextView; // displays correct answer

    private MediaPlayer whistleMediaPlayer; // incorrect sound
    private MediaPlayer correctMediaPlayer; // correct sound
    private boolean enableAudio = true; // play sound or not

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get audio preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String key = getString(R.string.enable_audio);
        enableAudio = sharedPreferences.getBoolean(key, true);

        // register listener for SharedPreferences changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        whistleMediaPlayer = MediaPlayer.create(getActivity(), R.raw.whistle); // load whistle (incorrect) sound
        correctMediaPlayer = MediaPlayer.create(getActivity(), R.raw.correct); // load correct sound
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        fileNameList = new ArrayList<>();
        quizAnimalsFileList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        // load the  animation when answered correctly
        correctAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.correct_animation);
        //correctAnimation.setRepeatCount(3);

        // Get GUI components
        quizLinearLayout =
                (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView =
                (TextView) view.findViewById(R.id.questionNumberTextView);
        animalImageView = (ImageView) view.findViewById(R.id.animalImageView);
        guessLinearLayouts = new LinearLayout[2];
        guessLinearLayouts[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // configure listeners for the guess Buttons
        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        // set questionNumberTextView's text
        questionNumberTextView.setText(
                getString(R.string.question, 1, NUM_OF_QUESTION_IN_GAME));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resetQuiz();
    }

    // set up and start the next quiz
    public void resetQuiz() {

        // Get the animal files names in assets folder
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear(); // empty list of image file names
        try {
            String[] paths = assets.list("animals");
            for (String path : paths) {
                Log.d(TAG, "resetQuiz():add to fileNameList=" + path);
                fileNameList.add(path.replace(".jpg", ""));
            }
        }
      catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0; // reset the number of correct answers made
        totalGuesses = 0; // reset the total number of guesses the user made
        quizAnimalsFileList.clear(); // clear prior list of quiz animals

        int animalCounter = 1;
        int numberOfAnimals = fileNameList.size();

        // add NUM_OF_QUESTION_IN_GAME random file names to the quizAnimalsFileList
        while (animalCounter <= NUM_OF_QUESTION_IN_GAME) {
            int randomIndex = random.nextInt(numberOfAnimals);

            // get the random file name
            String filename = fileNameList.get(randomIndex);

            // add animal if hasn't already been chosen
            if (!quizAnimalsFileList.contains(filename)) {
                Log.d(TAG, "resetQuiz():add to quizAnimalsFileList = " + filename);
                quizAnimalsFileList.add(filename); // add the file to the list
                ++animalCounter;
            }
        }

        loadNextAnimal(); // start the game by loading the first animal
    }

    // after the user guesses a correct animal, load the next question
    private void loadNextAnimal() {
        // get file name of the animal and remove it from the list
        String nextImageFileName = quizAnimalsFileList.remove(0);
        Log.d(TAG, "loadNextAnimal():nextImageFileName=" + nextImageFileName);
        correctAnswer = nextImageFileName.replace("_", " "); // update the correct answer
        Log.d(TAG, "loadNextAnimal():correctAnswer=" + correctAnswer);
        answerTextView.setText(""); // clear answerTextView

        // display current question number
        questionNumberTextView.setText(getString(
                R.string.question, (correctAnswers + 1), NUM_OF_QUESTION_IN_GAME));

        // use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        // get an InputStream to the asset representing the animal
        // and try to use the InputStream
        String path = "animals" + "/" + nextImageFileName + ".jpg";
        try (InputStream stream = assets.open(path)) {
            // load the asset as a Drawable and display on the animalImageView
            Drawable animalImage = Drawable.createFromStream(stream, nextImageFileName);
            animalImageView.setImageDrawable(animalImage);

            animate(false); // animate the animal onto the screen
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading " + path, exception);
        }

        Collections.shuffle(fileNameList); // shuffle file names

        // put the correct answer at the end of fileNameList
        int correct = fileNameList.indexOf(nextImageFileName);
        fileNameList.add(fileNameList.remove(correct));

        // add 4 guess Buttons with wrong answer
        for (int row = 0; row < 2; row++) {
            // place Buttons in currentTableRow
            for (int column = 0;
                 column < guessLinearLayouts[row].getChildCount();
                 column++) {
                // get reference to Button to configure
                Button newGuessButton =
                        (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);
                newGuessButton.setVisibility(View.VISIBLE);

                // get animal name and set it as newGuessButton's text
                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(filename.replace("_", " "));
            }
        }

        // randomly replace one Button with the correct answer
        int row = random.nextInt(2); // pick random row
        int column = random.nextInt(2); // pick random column
        LinearLayout randomRow = guessLinearLayouts[row]; // get the row
        ((Button) randomRow.getChildAt(column)).setText(correctAnswer);
    }

    // animates the entire quizLinearLayout on or off screen
    private void animate(boolean animateOut) {
        // prevent animation into the the UI for the first flag
        if (correctAnswers == 0)
            return;

        // calculate center x and center y
        int centerX = (quizLinearLayout.getLeft() +
                quizLinearLayout.getRight()) / 2; // calculate center x
        int centerY = (quizLinearLayout.getTop() +
                quizLinearLayout.getBottom()) / 2; // calculate center y

        // calculate animation radius
        int radius = Math.max(quizLinearLayout.getWidth(),
                quizLinearLayout.getHeight());

        Animator animator;

        // if the quizLinearLayout should animate out rather than in
        if (animateOut) {
            // create circular reveal animation
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(
                    new AnimatorListenerAdapter() {
                        // called when the animation finishes
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loadNextAnimal();
                        }
                    }
            );
        }
        else { // if the quizLinearLayout should animate in
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, 0, radius);
        }

        animator.setDuration(500); // set animation duration to 500 ms
        animator.start(); // start the animation
    }

    // called when a guess Button is touched
    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            Log.d(TAG, "guessButtonListener():guess=" + guess);
            Log.d(TAG, "guessButtonListener():correctAnswer=" + correctAnswer);
            ++totalGuesses; // increment number of guesses the user has made

            if (guess.equals(correctAnswer)) { // if the guess is correct
                ++correctAnswers; // increment the number of correct answers

                if (enableAudio)
                    correctMediaPlayer.start(); // play correct sound
                animalImageView.startAnimation(correctAnimation); // play correct animation

                // display correct answer in green text
                answerTextView.setText(correctAnswer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer,
                                getContext().getTheme()));

                disableButtons(); // disable all guess Buttons

                // if the user has correctly identified NUM_OF_QUESTION_IN_GAME flags
                if (correctAnswers == NUM_OF_QUESTION_IN_GAME) {
                    // display result then navigate back to main/first fragment
                    QuizResultDialogFragment quizResults = new QuizResultDialogFragment();
                    quizResults.setCancelable(false);
                    quizResults.show(getParentFragmentManager(), "quiz results");
                }
                else { // answer is correct but quiz is not over
                    // load the next flag after a 2-second delay
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    animate(true); // animate the flag off the screen
                                }
                            }, 2000); // 2000 milliseconds for 2-second delay
                }
            }
            else { // answer was incorrect

                // play sound
                if (enableAudio)
                    whistleMediaPlayer.start();

                // display "Incorrect!" in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(
                        R.color.incorrect_answer, getContext().getTheme()));

                // pop up dialog with "WRONG!" message
                Toast.makeText(getActivity(),
                        R.string.wrong,
                        Toast.LENGTH_SHORT).show();

                guessButton.setEnabled(false); // disable incorrect answer
                guessButton.setVisibility(View.INVISIBLE); // hide incorrect answer

            }
        }
    };

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

    // utility method that disables all answer Buttons
    private void disableButtons() {
        for (int row = 0; row < 2; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }

    // Return the number of guesses user made
    public int getTotalGuesses() {
        return totalGuesses;
    }

    // Navigate back to main/first fragment
    public void gotoFirstFragment() {
        NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // release media player resources
        if (whistleMediaPlayer != null) {
            whistleMediaPlayer.release();
            whistleMediaPlayer = null;
        }
        if (correctMediaPlayer != null) {
            correctMediaPlayer.release();
            correctMediaPlayer = null;
        }
    }
}