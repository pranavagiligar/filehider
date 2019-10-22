package com.quadcore.filehider;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HideListArrayAdapter extends ArrayAdapter<Item> {

	private Context c;
	private int id;
	private List<Item> items;
	private HideList hl;

	public HideListArrayAdapter(Context context, int textViewResourceId,
			List<Item> objects, HideList hl) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
		this.hl = hl;
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
			TextView t1 = (TextView) v.findViewById(R.id.TextView04);
			TextView t2 = (TextView) v.findViewById(R.id.TextView05);
			
			ImageView imageCity = (ImageView) v.findViewById(R.id.fd_Icon2);
			String uri = "drawable/" + o.getImage();
			int imageResource = c.getResources().getIdentifier(uri, null,
					c.getPackageName());
			Drawable image = c.getResources().getDrawable(imageResource);
			imageCity.setImageDrawable(image);

			if (t1 != null)
				t1.setText(o.getName());
			if (t2 != null)
				t2.setText(o.getPath());

			v.setOnClickListener(new OnItemClickListener(position));
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
			hl.onItemClick(mPosition);
			
		}
	}
}
