/*
 * Copyright 2015 Blanyal D'Souza.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.houssam.todolist;

import org.apache.commons.lang3.StringUtils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private TextToSpeech myTTS ;
    private SpeechRecognizer mysSpeechRecognizer ;
    public Intent inte;
    MaterialSearchView searchView;
    private RecyclerView mList;
    private SimpleAdapter mAdapter;
    private Toolbar mToolbar;
    private TextView mNoReminderView;
    private FloatingActionButton mAddReminderButton;
    private FloatingActionButton mAddReminderButtonVocal;
    private int mTempPost;
    private LinkedHashMap<Integer, Integer> IDmap = new LinkedHashMap<>();
    private ReminderDatabase rb;
    private MultiSelector mMultiSelector = new MultiSelector();
    private AlarmReceiver mAlarmReceiver;
    private Spinner spinner;



    Handler handler = new Handler();
    //MaterialSearchBar materialSearchBar ;
    List<String> suggestedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize reminder database
        rb = new ReminderDatabase(getApplicationContext());

        // Initialize views
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mAddReminderButton = (FloatingActionButton) findViewById(R.id.add_reminder);
        mAddReminderButtonVocal = (FloatingActionButton) findViewById(R.id.add_reminderVocal);
        mAddReminderButtonVocal.setImageResource(R.drawable.ic_action_voice_search);
        mList = (RecyclerView) findViewById(R.id.reminder_list);
        mNoReminderView = (TextView) findViewById(R.id.no_reminder_text);


        //materialSearchBar = (MaterialSearchBar) findViewById(R.id.SearchBar);

        //materialSearchBar.setHint("recherche");
        //materialSearchBar.setCardViewElevation(10);
        //loadSuggestedList();

        /*materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                List<String> suggest = new ArrayList<>();
                for (String search:suggestedList)
                {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))

                    {suggest.add(search);}



                }
                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/
        /*materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

                if(!(enabled))
                {
                    mAdapter = new SimpleAdapter(rb.getAllReminders());
                    mAdapter.setItemCount(getDefaultItemCount());
                    mList.setAdapter(mAdapter);
                }


            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                startSearch(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });*/



        // To check is there are saved reminders
        // If there are no reminders display a message asking the user to create reminders
        List<Reminder> mTest = rb.getAllReminders();

        if (mTest.isEmpty()) {
            mNoReminderView.setVisibility(View.VISIBLE);
        }

        // Create recycler view
        mList.setLayoutManager(getLayoutManager());
        registerForContextMenu(mList);
        mAdapter = new SimpleAdapter(1,"");
        mAdapter.setItemCount(getDefaultItemCount());
        mList.setAdapter(mAdapter);

        // Setup toolbar
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                mAdapter = new SimpleAdapter(1,"");
                mAdapter.setItemCount(getDefaultItemCount());
                mList.setAdapter(mAdapter);

            }
        });
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if( newText != null && !newText.isEmpty())
                {
                    mAdapter = new SimpleAdapter(0,newText);
                    mAdapter.setItemCount(getDefaultItemCount());
                    mList.setAdapter(mAdapter);
                }

                else {

                    mAdapter = new SimpleAdapter(1,"");
                    mAdapter.setItemCount(getDefaultItemCount());
                    mList.setAdapter(mAdapter);

                }

                return true ;

            }
        });




        // On clicking the floating action button
        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ReminderAddActivity.class);
                startActivity(intent);
            }
        });

          mAddReminderButtonVocal.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {

                  inte = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                  inte.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                          RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                  inte.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                  inte.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.FRANCE);

                  mysSpeechRecognizer.startListening(inte);




              }
          });

        // Initialize alarm
        mAlarmReceiver = new AlarmReceiver();

        initializeTextToSpeech();
        initializeSpeechRecognizer();


    }



    /*private void startSearch(String text) {
        mAdapter = new SimpleAdapter(rb.getReminders(text));
        mAdapter.setItemCount(getDefaultItemCount());
        mList.setAdapter(mAdapter);
    }*/

    /*private void loadSuggestedList() {

        suggestedList = rb.getTitles();
        materialSearchBar.setLastSuggestions(suggestedList);

    }*/



    private void initializeSpeechRecognizer() {

        if(SpeechRecognizer.isRecognitionAvailable(this))
        {
            Toast.makeText(MainActivity.this, " available",Toast.LENGTH_LONG).show();

            mysSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mysSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {
                    String TAG = "TAG";

                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            Log.e(TAG, "ERROR_AUDIO");
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            Log.e(TAG, "ERROR_CLIENT");
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            Log.e(TAG, "ERROR_INSUFFICIENT_PERMISSIONS");
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            Log.e(TAG, "ERROR_NETWORK");
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            Log.e(TAG, "ERROR_NETWORK_TIMEOUT");
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            Log.e(TAG, "ERROR_RECOGNIZER_BUSY");
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            Log.e(TAG, "ERROR_SERVER");
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            Log.v(TAG, "ERROR_NO_MATCH");
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            Log.v(TAG, "ERROR_SPEECH_TIMEOUT");
                            break;
                        default:
                            Log.v(TAG, "ERROR_UNKOWN");
                    }

                }

                @Override
                public void onResults(Bundle bundle) {

                    List<String> results = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );
                    processresult(results.get(0));


                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }

        else{
            Toast.makeText(MainActivity.this, "is not available",Toast.LENGTH_LONG).show();

        }


    }

    private void processresult(String command) {



        command = command.toLowerCase();
         String titre = "dada";
         String desc = "dada";

        Toast.makeText(MainActivity.this, command,Toast.LENGTH_LONG).show();





        //ajouter une tache

        if(command.indexOf("ajoute")!=-1 || command.indexOf("ajout")!=-1 ||
                command.indexOf("ajoutez")!=-1  || command.indexOf("ajouter")!=-1 ){



            if(command.indexOf("titre")==-1){

                titre  = StringUtils.substringBetween(command,"titre","description");
                speak("Le titre de votre tâche est "+titre);
                Intent myIntent = new Intent(this, ReminderAddActivity.class);
                myIntent.putExtra("titreIntent",titre);
                startActivity(myIntent);

            }
            /*
            else{
                if(command.indexOf("description")!=-1){
                    var description =  command.substringAfter("description")
                    var commandArr = stringToWords(command)
                    command.replaceAfter(commandArr.get(commandArr.indexOf("et")),"")
                    bundle.putString("Description",description)
                }
                titre =command.substringAfter("tâche")
                speak("Le titre de votre tâche est "+titre)
                var myFrag : Fragment = AddTaskFragment()
                bundle.putString("Titre", titre)
                myFrag.setArguments(bundle)
                navigation.setSelectedItemId(R.id.navigation_home);
                loadFragment(myFrag)
                return
            }*/
        }


        if(command.indexOf("supprime")!=-1 || command.indexOf("supprim")!=-1 ||
                command.indexOf("supprimez")!=-1  || command.indexOf("supprimer")!=-1 ){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rb.deleteAllReminders();
                    finish();
                    startActivity(getIntent());                }
            }, 3000);

        }


        // que peux tu faire ?

        if(command.indexOf("que peux") != -1)
        {
            speak("Je peux : Supprimer toutes vos tâches , Quitter l'application, Ajouter une tâche avec ou sans titre et afficher la liste des tâches");
        }


        //closing app

        if(command.indexOf("arrête")!=-1){
            speak("Merci d'avoir utiliser notre application");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                finish();
                 }
            }, 3000);
        }




    }


        // supprimer tous les taches

        // afficher les taches




    private void initializeTextToSpeech()
    {

        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size() == 0)
                {
                    Toast.makeText(MainActivity.this, "There is no TTS engine",Toast.LENGTH_LONG).show();
                    finish();
                }
                else {
                    myTTS.setLanguage(Locale.FRANCE);
                    speak("Bienvenue Dans l'application Todo List");
                }
            }
        });

    }

    private void speak(String message) {

        if(Build.VERSION.SDK_INT >= 21 ) {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null , null );

        }

        else {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH,null);
        }

    }

    // Create context menu for long press actions
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
    }

    // Multi select items in recycler view
    private android.support.v7.view.ActionMode.Callback mDeleteMode = new ModalMultiSelectorCallback(mMultiSelector) {

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void requestRecordAudioPermission() {

            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (MainActivity.this.checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED) {

            } else {

                Toast.makeText(MainActivity.this, "This app needs to record audio through the microphone....", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{requiredPermission}, 101);
            }}


            @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                // On clicking discard reminders
                case R.id.discard_reminder:
                    // Close the context menu
                    actionMode.finish();

                    // Get the reminder id associated with the recycler view item
                    for (int i = IDmap.size(); i >= 0; i--) {
                        if (mMultiSelector.isSelected(i, 0)) {
                            int id = IDmap.get(i);

                            // Get reminder from reminder database using id
                            Reminder temp = rb.getReminder(id);
                            // Delete reminder
                            rb.deleteReminder(temp);
                            // Remove reminder from recycler view
                            mAdapter.removeItemSelected(i);
                            // Delete reminder alarm
                            mAlarmReceiver.cancelAlarm(getApplicationContext(), id);
                        }
                    }

                    // Clear selected items in recycler view
                    mMultiSelector.clearSelections();
                    // Recreate the recycler items
                    // This is done to remap the item and reminder ids
                    mAdapter.onDeleteItem(getDefaultItemCount());

                    // Display toast to confirm delete
                    Toast.makeText(getApplicationContext(),
                            "Deleted",
                            Toast.LENGTH_SHORT).show();

                    // To check is there are saved reminders
                    // If there are no reminders display a message asking the user to create reminders
                    List<Reminder> mTest = rb.getAllReminders();

                    if (mTest.isEmpty()) {
                        mNoReminderView.setVisibility(View.VISIBLE);
                    } else {
                        mNoReminderView.setVisibility(View.GONE);
                    }

                    return true;

                // On clicking save reminders
                case R.id.save_reminder:
                    // Close the context menu
                    actionMode.finish();
                    // Clear selected items in recycler view
                    mMultiSelector.clearSelections();
                    return true;

                default:
                    break;
            }
            return false;
        }
    };

    // On clicking a reminder item
    private void selectReminder(int mClickID) {
        String mStringClickID = Integer.toString(mClickID);

        // Create intent to edit the reminder
        // Put reminder id as extra
        Intent i = new Intent(this, ReminderEditActivity.class);
        i.putExtra(ReminderEditActivity.EXTRA_REMINDER_ID, mStringClickID);
        startActivityForResult(i, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAdapter.setItemCount(getDefaultItemCount());
    }

    // Recreate recycler view
    // This is done so that newly created reminders are displayed
    @Override
    public void onResume(){

        super.onResume();

        // To check is there are saved reminders
        // If there are no reminders display a message asking the user to create reminders
        List<Reminder> mTest = rb.getAllReminders();

        if (mTest.isEmpty()) {
            mNoReminderView.setVisibility(View.VISIBLE);
        } else {
            mNoReminderView.setVisibility(View.GONE);
        }

        mAdapter.setItemCount(getDefaultItemCount());
    }

    // Layout manager for recycler view
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }

    protected int getDefaultItemCount() {
        return 100;
    }

    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    // Setup menu



    public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.VerticalItemHolder> {
        private ArrayList<ReminderItem> mItems;
        private int c ;
        private String text ;

        public SimpleAdapter(int var,String txt) {
            mItems = new ArrayList<>();
            c = var;
            text = txt ;
        }

        public void setItemCount(int count) {
            mItems.clear();
            mItems.addAll(generateData(count));
            notifyDataSetChanged();
        }

        public void onDeleteItem(int count) {
            mItems.clear();
            mItems.addAll(generateData(count));
        }

        public void removeItemSelected(int selected) {
            if (mItems.isEmpty()) return;
            mItems.remove(selected);
            notifyItemRemoved(selected);
        }

        // View holder for recycler view items
        @Override
        public VerticalItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            View root = inflater.inflate(R.layout.recycle_items, container, false);

            return new VerticalItemHolder(root, this);
        }

        @Override
        public void onBindViewHolder(VerticalItemHolder itemHolder, int position) {
            ReminderItem item = mItems.get(position);
            itemHolder.setReminderTitle(item.mTitle);
            itemHolder.setReminderDesc(item.mDesc);
            itemHolder.setRemindertype(item.mType);
            itemHolder.setReminderDateTime(item.mDateTime);
            itemHolder.setReminderRepeatInfo(item.mRepeat, item.mRepeatNo, item.mRepeatType);
            itemHolder.setActiveImage(item.mActive);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        // Class for recycler view items
        public  class ReminderItem {
            public String mTitle;
            public String mDesc;
            public String mType;
            public String mDateTime;
            public String mRepeat;
            public String mRepeatNo;
            public String mRepeatType;
            public String mActive;

            public ReminderItem(String Title, String Desc, String Type , String DateTime, String Repeat, String RepeatNo, String RepeatType, String Active) {
                this.mTitle = Title;
                this.mDesc = Desc;
                this.mType = Type;
                this.mDateTime = DateTime;
                this.mRepeat = Repeat;
                this.mRepeatNo = RepeatNo;
                this.mRepeatType = RepeatType;
                this.mActive = Active;
            }
        }

        // Class to compare date and time so that items are sorted in ascending order
        public class DateTimeComparator implements Comparator {
            DateFormat f = new SimpleDateFormat("dd/mm/yyyy hh:mm");

            public int compare(Object a, Object b) {
                String o1 = ((DateTimeSorter)a).getDateTime();
                String o2 = ((DateTimeSorter)b).getDateTime();

                try {
                    return f.parse(o1).compareTo(f.parse(o2));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        // UI and data class for recycler view items
        public  class VerticalItemHolder extends SwappingHolder
                implements View.OnClickListener, View.OnLongClickListener {
            private TextView mTitleText, mDateAndTimeText, mRepeatInfoText, mDescText , mTypetext ;
            private ImageView mActiveImage , mThumbnailImage;
            private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;
            private TextDrawable mDrawableBuilder;
            private SimpleAdapter mAdapter;

            public VerticalItemHolder(View itemView, SimpleAdapter adapter) {
                super(itemView, mMultiSelector);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                itemView.setLongClickable(true);

                // Initialize adapter for the items
                mAdapter = adapter;

                // Initialize views
                mTitleText = (TextView) itemView.findViewById(R.id.recycle_title);
                mDescText = (TextView) itemView.findViewById(R.id.recycle_description);
                mTypetext = (TextView) itemView.findViewById(R.id.recycle_type);
                mDateAndTimeText = (TextView) itemView.findViewById(R.id.recycle_date_time);
                mRepeatInfoText = (TextView) itemView.findViewById(R.id.recycle_repeat_info);
                mActiveImage = (ImageView) itemView.findViewById(R.id.active_image);
                mThumbnailImage = (ImageView) itemView.findViewById(R.id.thumbnail_image);
            }

            // On clicking a reminder item
            @Override
            public void onClick(View v) {
                if (!mMultiSelector.tapSelection(this)) {
                    mTempPost = mList.getChildAdapterPosition(v);

                    int mReminderClickID = IDmap.get(mTempPost);
                    selectReminder(mReminderClickID);

                } else if(mMultiSelector.getSelectedPositions().isEmpty()){
                    mAdapter.setItemCount(getDefaultItemCount());
                }
            }

            // On long press enter action mode with context menu
            @Override
            public boolean onLongClick(View v) {
                AppCompatActivity activity = MainActivity.this;
                activity.startSupportActionMode(mDeleteMode);
                mMultiSelector.setSelected(this, true);
                return true;
            }

            // Set reminder title view
            public void setReminderTitle(String title) {
                mTitleText.setText(title);
                String letter = "A";

                if(title != null && !title.isEmpty()) {
                    letter = title.substring(0, 1);
                }

                // Create a circular icon consisting of  a random background colour and first letter of title
                mDrawableBuilder = TextDrawable.builder()
                        .buildRound(letter,Color.GRAY);
                mThumbnailImage.setImageDrawable(mDrawableBuilder);
            }

            public void setReminderDesc(String desc) {
                mDescText.setText(desc);

            }

            public void setRemindertype(String type) {
                mTypetext.setText(type);

            }


            // Set date and time views
            public void setReminderDateTime(String datetime) {
                mDateAndTimeText.setText(datetime);
            }

            // Set repeat views
            public void setReminderRepeatInfo(String repeat, String repeatNo, String repeatType) {
                if(repeat.equals("true")){
                    mRepeatInfoText.setText("Every " + repeatNo + " " + repeatType + "(s)");
                }else if (repeat.equals("false")) {
                    mRepeatInfoText.setText("Repeat Off");
                }
            }

            // Set active image as on or off
            public void setActiveImage(String active){
                if(active.equals("true")){
                    mActiveImage.setImageResource(R.drawable.ic_notifications_on_white_24dp);
                }else if (active.equals("false")) {
                    mActiveImage.setImageResource(R.drawable.ic_notifications_off_grey600_24dp);
                }
            }
        }

        // Generate random test data
        public  ReminderItem generateDummyData() {
            return new ReminderItem("1", "2", "3", "4", "5", "6","7","8");
        }

        // Generate real data for each item
        public List<ReminderItem> generateData(int count) {
            ArrayList<SimpleAdapter.ReminderItem> items = new ArrayList<>();

            // Get all reminders from the database

                List<Reminder> reminders = new ArrayList<>();


            if(c==0) {
                reminders = rb.getReminders(text);
            }
            else {
                reminders = rb.getAllReminders();
            }




            // Initialize lists
            List<String> Titles = new ArrayList<>();
            List<String> Descs = new ArrayList<>();
            List<String> Types = new ArrayList<>();
            List<String> Repeats = new ArrayList<>();
            List<String> RepeatNos = new ArrayList<>();
            List<String> RepeatTypes = new ArrayList<>();
            List<String> Actives = new ArrayList<>();
            List<String> DateAndTime = new ArrayList<>();
            List<Integer> IDList= new ArrayList<>();
            List<DateTimeSorter> DateTimeSortList = new ArrayList<>();

            // Add details of all reminders in their respective lists
            for (Reminder r : reminders) {
                Titles.add(r.getTitle());
                Descs.add(r.getDesc());
                Types.add(r.getType());
                DateAndTime.add(r.getDate() + " " + r.getTime());
                Repeats.add(r.getRepeat());
                RepeatNos.add(r.getRepeatNo());
                RepeatTypes.add(r.getRepeatType());
                Actives.add(r.getActive());
                IDList.add(r.getID());
            }

            int key = 0;

            // Add date and time as DateTimeSorter objects
            for(int k = 0; k<Titles.size(); k++){
                DateTimeSortList.add(new DateTimeSorter(key, DateAndTime.get(k)));
                key++;
            }

            // Sort items according to date and time in ascending order
            Collections.sort(DateTimeSortList, new DateTimeComparator());

            int k = 0;

            // Add data to each recycler view item
            for (DateTimeSorter item:DateTimeSortList) {
                int i = item.getIndex();

                items.add(new SimpleAdapter.ReminderItem(Titles.get(i),Descs.get(i),Types.get(i), DateAndTime.get(i), Repeats.get(i),
                        RepeatNos.get(i), RepeatTypes.get(i), Actives.get(i)));
                IDmap.put(k, IDList.get(i));
                k++;
            }
            return items;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTTS.shutdown();
    }
}
