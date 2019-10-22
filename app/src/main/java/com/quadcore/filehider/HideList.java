package com.quadcore.filehider;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class HideList extends ListActivity {
	
	private HideListArrayAdapter adapter;
	public ArrayList<FileAttributeHolder> holder = new ArrayList<FileAttributeHolder>();
	
	private boolean isBackPressed = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fill();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(!isBackPressed) {
			setResult(7931);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		isBackPressed = true;	
		setResult(1397);
		finish();
	}

	private void fill() {
		this.setTitle("Hidden Files");
		DatabaseHandlerFile dbFile = new DatabaseHandlerFile(this);
		holder = dbFile.retrieveField();
		ArrayList<Item> hiddenItems = new ArrayList<Item>();
		for(FileAttributeHolder hdr : holder) {
			hiddenItems.add(new Item(hdr.getFileName(), null, null, hdr.getPath(), "encrypted"));
		}
		
		adapter = new HideListArrayAdapter(this, R.layout.hide_view, hiddenItems, this);
		this.setListAdapter(adapter);
	}
	
	public void onItemClick(int mPosition) {
		ConfirmDialogFragment cdf = new ConfirmDialogFragment();
		cdf.setPosAndContext(mPosition, this);
		cdf.show(getFragmentManager(), "FragmentManager");
	}
	
	public static class ConfirmDialogFragment extends DialogFragment {
		
		private int position;
		private HideList context;
		public ConfirmDialogFragment(){}
		/*public ConfirmDialogFragment(int pos, Context context) {
			position = pos;
			this.context = context;
		}*/
		public void setPosAndContext(int pos, HideList context) {
			position = pos;
			this.context = context;
		}
		
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage("Do you want to unhide")
	               .setPositiveButton("YES", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                      try {
							new Crypt(context).decrypt(context.holder.get(position));
							DatabaseHandlerFile dbFile = new DatabaseHandlerFile(context);
							dbFile.deleteField(context.holder.get(position).getPath());
							dbFile.close();
							File file = new File(context.holder.get(position).getCurrentPath()+context.holder.get(position).getUnique_id());
							file.delete();
                              context.fill();
						} catch (Exception e) {
							e.printStackTrace();
						}
	                   }
	               })
	               .setNegativeButton("NO", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       dismiss();
	                   }
	               });
	        return builder.create();
	    }
	}
}
