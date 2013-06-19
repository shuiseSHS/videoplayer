package comhappy.live.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.happy.live.R;
import com.happy.live.entity.PromoInfo;

public class PromoAdapter extends BaseAdapter {

	List<PromoInfo> data;

	Context context;

	public PromoAdapter(Context context, List<PromoInfo> data) {
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
			convertView = inflater.inflate(R.layout.promo_item, null);
			vh = new ViewHolder();
			vh.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
			vh.txt_time = (TextView) convertView.findViewById(R.id.txt_time);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}

		vh.txt_name.setText(data.get(position).name);
		vh.txt_time.setText(data.get(position).time);
		return convertView;
	}

	public class ViewHolder {
		TextView txt_name;
		TextView txt_time;
	}
}