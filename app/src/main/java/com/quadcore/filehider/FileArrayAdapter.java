package com.quadcore.filehider;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileArrayAdapter extends ArrayAdapter<Item> {

	private Context c;
	private int id;
	private List<Item> items;
	private FileExplorer fe;

	public FileArrayAdapter(Context context, int textViewResourceId,
			List<Item> objects, FileExplorer fe) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
		this.fe = fe;
	}

	public Item getItem(int i) {
		return items.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}

		final Item o = items.get(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.TextView01);
			TextView t2 = (TextView) v.findViewById(R.id.TextView02);
			TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);

			ImageView imageCity = (ImageView) v.findViewById(R.id.fd_Icon1);
			String uri = "drawable/" + o.getImage();
			int imageResource = c.getResources().getIdentifier(uri, null,
					c.getPackageName());
			Drawable image = c.getResources().getDrawable(imageResource);
			imageCity.setImageDrawable(image);

			if (t1 != null)
				t1.setText(o.getName());
			if (t2 != null)
				t2.setText(o.getData());
			if (t3 != null)
				t3.setText(o.getDate());

			v.setOnClickListener(new OnItemClickListener(position));
			if(!"directory_up".equals(o.getImage()))
				v.setOnLongClickListener(new OnItemLongClickListener(position));
		}
		return v;
	}

	/********* Called when Item click in ListView ************/
	private class OnItemClickListener implements OnClickListener {
		private int mPosition;

		OnItemClickListener(int position) {
			mPosition = position;
		}

		@Override
		public void onClick(View view) {
			fe.onItemClick(mPosition);
			
		}
	}

	private class OnItemLongClickListener implements OnLongClickListener {
		private int mPosition;

		OnItemLongClickListener(int position) {
			mPosition = position;
		}

		@Override
		public boolean onLongClick(View view) {
			fe.isLongClicked = true;
			fe.onItemLongClick(mPosition, view);
			return false;
		}
	}
}
