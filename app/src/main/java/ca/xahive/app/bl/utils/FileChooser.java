package ca.xahive.app.bl.utils;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.ui.views.NavigationBar;

public class FileChooser extends Activity {
    public static final String SELECT_FILE_INTENT_KEY = "filePath";

    Stack<File> dirStack = new Stack<File>();

    private File currentDir;
    private FileArrayAdapter adapter;


    private ListView getFileListView() {
        return (ListView)findViewById(R.id.fileListView);
    }

    private NavigationBar getNavBar() {
        return (NavigationBar)findViewById(R.id.navBar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list_view);
        setupFileListListener();
        currentDir = new File("/");
        fill(currentDir);
    }

    private void fill(File f)
    {
        File[]dirs = f.listFiles();
        this.setTitle(String.format(getString(R.string.file_chooser_title_format), f.getName()));
        List<SelectedFileOption> dir = new ArrayList<SelectedFileOption>();
        List<SelectedFileOption>fls = new ArrayList<SelectedFileOption>();
        try{
            for(File ff: dirs)
            {
                if(ff.isDirectory()) {

                    if(!ff.isHidden()) {
                        dir.add(new SelectedFileOption(ff.getName(), getString(R.string.folder_label), ff.getAbsolutePath()));
                    }

                } else if (!ff.isHidden()) {
                    SelectedFileOption sfo = new SelectedFileOption(
                            ff.getName(),
                            String.format(getString(R.string.file_size_label_format), Helpers.humanReadableFileSize(ff.length())),
                            ff.getAbsolutePath()
                    );
                    fls.add(sfo);
                }
            }
        }catch(Exception e)
        {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);


        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_list_cell,dir);
        getFileListView().setAdapter(adapter);
        getNavBar().configNavBarWithTitle(currentDir.getAbsolutePath());

    }

    private void setupFileListListener() {

        getFileListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelectedFileOption o = adapter.getItem(position);
                if(o.getData().equalsIgnoreCase("folder")){
                    dirStack.push(currentDir);
                    currentDir = new File(o.getPath());
                    fill(currentDir);
                }
                else if (o.getData().equalsIgnoreCase("parent directory")) {
                    currentDir = dirStack.pop();
                    fill(currentDir);
                } else {
                    onFileClick(o);
                }
            }
        });

    }

    private void onFileClick(SelectedFileOption o)
    {
        Intent returnIntent = new Intent();

        returnIntent.putExtra(SELECT_FILE_INTENT_KEY, o.getPath());
        setResult(RESULT_OK, returnIntent);
        finish();
    }


    @Override
    public void onBackPressed() {
        if (dirStack.size() == 0)
        {
            finish();
            return;
        }
        currentDir = dirStack.pop();
        fill(currentDir);
    }




}
