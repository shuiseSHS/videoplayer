package comhappy.live.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.happy.live.R;
import com.happy.live.entity.TURL;
import com.happy.live.net.BitmapLoader;

public class ListAdapter extends BaseAdapter {

	List<TURL> data;
	Context context;

	public ListAdapter(Context context, List<TURL> data) {
		this.data = data;
		this.context = context;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.item, null);
			vh = new ViewHolder();
			vh.txt_url = (TextView) convertView.findViewById(R.id.txt_url);
			vh.txt_program = (TextView) convertView.findViewById(R.id.current_program);
			vh.img_ico = (ImageView) convertView.findViewById(R.id.img_ico);
			vh.programlist = (ListView) convertView.findViewById(R.id.programList);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}
		
		vh.turl = data.get(position);
		vh.txt_url.setText(vh.turl.name);
		vh.txt_program.setText("节目预告");
		vh.programlist.setVisibility(View.GONE);
		vh.img_ico.setImageResource(R.drawable.default_ico);
		BitmapLoader.loadBitmap(vh.img_ico, data.get(position).icon);
		return convertView;
	}
	
	public class ViewHolder {
		TextView txt_url, txt_program;
		ImageView img_ico;
		public TURL turl;
		public ListView programlist;
	}

}
