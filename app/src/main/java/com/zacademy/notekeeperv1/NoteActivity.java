package com.zacademy.notekeeperv1;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

    /************************************** Fields ************************************************/

    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_POSITION = "com.zacademy.notekeeperv1.NOTE_INFO_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNewNotePosition;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;

    /************************************** Overrided Methods *************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));

        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if (mViewModel.mIsNewlyCreated && savedInstanceState != null) {
            mViewModel.restoreState(savedInstanceState);
        }
        mViewModel.mIsNewlyCreated = false;

        mSpinnerCourses = findViewById(R.id.spinner_courses);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        //Create adapter to associate courses list with spinnerCourses Spinner.
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                courses);//list values read in by CourseInfo.toString()
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        //Receive Intent from NoteListActivity; extract its extra; use the extra(Parcelable, then later position) to display the selected note from NoteListActivity in NoteActivity
        readDisplayStateValues();
        saveOriginalNoteValues();


        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);


        //use intent extra values capture by readDisplayState, to display the note;
        if (!mIsNewNote) {
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        }
        Log.d(TAG, "NoteActivity onCreate() called");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.d("CALLED", "NoteActivity onStart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d("CALLED", "NoteActivity onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            Log.i(TAG, "Cancelling note at position: " + mNewNotePosition);
            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNewNotePosition);
            } else {
                storePreviousNoteValues();
            }

        } else {
            saveNote();
        }

        Log.d(TAG, "NoteActivity onPause() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.d("CALLED", "NoteActivity onStop() called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.d("CALLED", "NoteActivity onRestart() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d("CALLED", "NoteActivity onDestroy() called");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null)
            mViewModel.saveState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    /************************************** User Defined Methods **********************************/

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNewNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = mNewNotePosition == POSITION_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }

        Log.i(TAG, "mNoteNewPosition: " + mNewNotePosition);
        mNote = DataManager.getInstance().getNotes().get(mNewNotePosition);//make it member variable so that displayNotes() can also access it// List.get(position): Returns the element at the specified position in this list.

    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse()); // get the reference of the CourseInfo object in this NoteInfo from intent.getParcelableExtra(NOTE_INFO). get the index of that CourseInfo object in the courses List
        spinnerCourses.setSelection(courseIndex); //use that index to set the current selection  in the spinnerCourses spinner

        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out what I learned in the pluralsight \"" + course.getTitle() + "\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    private void saveNote() { // set the values of the note we currently have reference to
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNewNotePosition = dm.createNewNote();
//        mNote = dm.getNotes().get(mNewNotePosition);
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;
        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();

    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }


}