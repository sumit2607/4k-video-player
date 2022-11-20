package com.demo.mxplayer.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.demo.mxplayer.R;
import com.demo.mxplayer.adapter.FileAdapter;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.download.FileDetailDialog;
import com.demo.mxplayer.models.SingleFile;

import java.io.File;
import java.util.ArrayList;

import static com.demo.mxplayer.activity.MainActivity.mDownloaderService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;

public class Download_list extends Fragment {

    ListView listFiles;
    ArrayList<SingleFile> files;
    ArrayList<Integer> selectedIds = new ArrayList();
    boolean selectionMode = false;
    boolean deleteFiles = false;
    ActionMode mMode;
    FileAdapter adapter;
    AlertDialog alert;
    TextView notfound;
    private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        private AnActionModeOfEpicProportions() {
        }


        public void onDestroyActionMode(ActionMode actionMode) {
            for (int i = 0; i < files.size(); i++) {
                ((SingleFile)files.get(i)).isSelected = false;
            }
           adapter.refreshRecords(files);
           selectedIds.removeAll(selectedIds);
            selectionMode = false;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            CharSequence[] charSequenceArr = new CharSequence[]{getActivity().getString(R.string.remove_history_details)};
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), (int) R.style.AlertDialogCustom));
            builder.setTitle(getActivity().getString(R.string.remove_history_title));
            builder.setMultiChoiceItems(charSequenceArr, null, new DialogInterface.OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialogInterface, int i, boolean z) {
                    if (z) {
                       deleteFiles = true;
                    } else {
                        deleteFiles = false;
                    }
                }
            }).setPositiveButton(getActivity().getString(R.string.string_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    for (i = 0; i < files.size(); i++) {
                        ((SingleFile) files.get(i)).isSelected = false;
                    }
                   mMode.finish();
                   adapter.refreshRecords(files);
                }
            }).setNegativeButton(getActivity().getString(R.string.string_delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                   alert.dismiss();
                    DbHandler.openDB();
                    for (int i2 = 0; i2 < files.size(); i2++) {
                        if (selectedIds.contains(new Integer(((SingleFile)files.get(i2)).id))) {
                            if (deleteFiles) {
                                String str = "Downloadlist";
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Delete File: ");
                                stringBuilder.append(((SingleFile) files.get(i2)).path);
                                stringBuilder.append(File.separator);
                                stringBuilder.append(((SingleFile) files.get(i2)).title);
                                Log.i(str, stringBuilder.toString());
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(((SingleFile) files.get(i2)).path);
                                stringBuilder.append(File.separator);
                                stringBuilder.append(((SingleFile) files.get(i2)).title);
                                File file = new File(stringBuilder.toString());
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                            DbHandler.removeFile(((SingleFile)files.get(i2)).id);
                        }
                    }
                  files = DbHandler.getCompletedFiles();
                    DbHandler.closeDB();
                    mMode.finish();
                }
            });
          alert = builder.create();
            alert.show();
            return true;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            menu.add("Delete").setIcon(R.drawable.action_delete);
            return true;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_download_list, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.listFiles = (ListView)view. findViewById(R.id.list_download_history);
        this.notfound = (TextView)view. findViewById(R.id.lbl_no_item_found);
        DbHandler.openDB();
        this.files = DbHandler.getCompletedFiles();
        this.adapter = new FileAdapter(getActivity(), R.layout.single_file_detail, this.files);
        this.listFiles.setAdapter(adapter);
if(files.size()<= 0){
    listFiles.setVisibility(View.GONE);
    notfound.setVisibility(View.VISIBLE);


}
        this.listFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                if (selectionMode) {
                    if (((SingleFile) files.get(i)).isSelected) {
                        ((SingleFile) files.get(i)).isSelected = false;
                        view.setBackgroundColor(0);
                    } else {
                        ((SingleFile) files.get(i)).isSelected = true;
                        view.setBackgroundColor(Color.parseColor("#EAEAEA"));
                    }
                    i = (int) j;
                    if (selectedIds.contains(new Integer(i))) {
                        selectedIds.remove(new Integer(i));
                    } else {
                       selectedIds.add(new Integer(i));
                    }
                    if (selectedIds.size() <= 0) {
                       mMode.finish();
                        return;
                    }
                    return;
                }
                new FileDetailDialog(getActivity(), mDownloaderService, (int) j).show();
            }
        });
        this.listFiles.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long j) {
                if ((files.get(i)).isSelected) {
                    ((SingleFile) files.get(i)).isSelected = false;
                    view.setBackgroundColor(0);
                } else {
                    ((SingleFile) files.get(i)).isSelected = true;
                    view.setBackgroundColor(Color.parseColor("#EAEAEA"));
                }
                if (selectedIds.size() <= 0) {
                    AppCompatActivity activity=(AppCompatActivity)getActivity();
                    mMode = activity.startSupportActionMode(new AnActionModeOfEpicProportions());
                   mMode.setTitle(getActivity().getString(R.string.drawer_menu_completed));
                }
                i = (int) j;
                if (selectedIds.contains(new Integer(i))) {
                   selectedIds.remove(new Integer(i));
                } else {
                   selectedIds.add(new Integer(i));
                }
                if (selectedIds.size() <= 0) {
                    mMode.finish();
                }
               selectionMode = true;
                return true;
            }
        });
    }



}
