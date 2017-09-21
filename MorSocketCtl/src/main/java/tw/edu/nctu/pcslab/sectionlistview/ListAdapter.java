package tw.edu.nctu.pcslab.sectionlistview;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import tw.edu.nctu.pcslab.socketctl.R;

public class ListAdapter extends ArrayAdapter {

    LayoutInflater inflater;
    public ListAdapter(Context context, ArrayList items) {
        super(context, 0, items);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        DeviceCell cell = (DeviceCell) getItem(position);

        //If the cell is a section header we inflate the header layout
        if(cell.isSectionHeader())
        {
            v = inflater.inflate(R.layout.device_list_view_section_header, null);

            v.setClickable(false);

            TextView header = (TextView) v.findViewById(R.id.section_header);
            header.setText(cell.getName());
        }
        else
        {
            v = inflater.inflate(R.layout.device_row_view, null);
            TextView name = (TextView) v.findViewById(R.id.device_row_text_view);
//            TextView category = (TextView) v.findViewById(R.id.category);
            name.setText(cell.getName());
//            category.setText(cell.getCategory());
        }
        return v;
    }
}