package com.hazzard.nathan.to_do;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ArrayList<Task> taskList;
    final static String filename = "taskList";
    public Notifier notifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notifier = new Notifier(this);

        //displayText is where all the tasks are currently listed
        TextView displayText = (TextView) findViewById(R.id.body);
        displayText.setMovementMethod(new ScrollingMovementMethod());

        //This adds the navpane to the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Sets the floating action button to create new tasks
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newTask = new Intent(MainActivity.this, TaskCreator.class);
                MainActivity.this.startActivity(newTask);
            }
        });

        //Does some android magic around the navpane
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //Loads the task list fresh from memory every time control goes back to this activity
    @Override
    protected void onResume() {
        super.onResume();
        taskList = loadTaskList();
        //TODO implement a method to sortList by whatever it was last sorted by (Probably Preferences)
        sortList("name");
        displayList();
        //TODO remove this test
        notifier.taskNotification(taskList.get(0));
    }

    //Loads the taskList object from memory
    public ArrayList loadTaskList() {
        ArrayList list = new ArrayList();
        try {
            FileInputStream inputStream = openFileInput(filename);
            byte[] byteBuffer = new byte[inputStream.available()];
            inputStream.read(byteBuffer);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            list = (ArrayList) objectIn.readObject();
        } catch (Exception e) {
        }
        return list;
    }

    //Sorts the taskList according to a selection of comparators, chosen by the value of sort
    public void sortList(String sort)
    {
        switch (sort){
            case "name":
                Collections.sort(taskList, new Task.NameComparator());
                break;
            case "date":
                Collections.sort(taskList, new Task.DueDateComparator());
                break;
            case "priority":
                Collections.sort(taskList, new Task.PriorityComparator());
                break;
            default:
                break;
        }
    }

    //iterates through taskList and adds every Task to displayText
    public void displayList() {
        TextView displayText = (TextView) findViewById(R.id.body);
        String taskText = "";
        for (int i = 0; i < taskList.size(); i++) {
            taskText += taskList.get(i).toString();
        }
        displayText.setText(taskText);
    }

    //Overrides the back button to close the navpane if it's open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Android magic
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Definitely more Android magic
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //This is where the navpane items are defined
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Called when an nav option is clicked, this determines what option was chosen, and reacts
        //Currently only calls my sort operations
        switch(item.getItemId()) {
            case R.id.sort_name:
                sortList("name");
                Toast.makeText(MainActivity.this, "Sorted Alphabetically", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sort_date:
                sortList("date");
                Toast.makeText(MainActivity.this, "Sorted by Date", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sort_priority:
                sortList("priority");
                Toast.makeText(MainActivity.this, "Sorted by Priority", Toast.LENGTH_SHORT).show();
                break;
        }
        //Refreshes the displayList after it's sorted
        displayList();

        //Closes the navpane afterwards
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
