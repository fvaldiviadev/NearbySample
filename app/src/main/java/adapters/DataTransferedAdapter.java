package adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import fvaldiviadev.nearbysample.R;
import pojo.DataTransfered;

/**
 * Created by Fran on 26/01/2018.
 */

public class DataTransferedAdapter  extends ArrayAdapter {

    private Context mContext;
    private int resourceID;
    private List<DataTransfered> listDataTransfered;
    private DataTransfered promo;

    public DataTransferedAdapter(Context context, int resource, List<DataTransfered> listDataTransfered){
        super(context, resource, listDataTransfered);
        this.mContext = context;
        this.resourceID = resource;
        this.listDataTransfered = listDataTransfered;
        this.promo = promo;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return listDataTransfered.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(resourceID, parent, false);

        ImageView ivTitulo = view.findViewById(R.id.iv_image_datatransfered);
        TextView tvTitulo = view.findViewById(R.id.tv_text_datatransfered);

        if(listDataTransfered!=null) {
            tvTitulo.setText(listDataTransfered.get(position).getMessage());
            byte[] imageByteArray = Base64.decode(listDataTransfered.get(position).getImageBase64(), Base64.DEFAULT);

            ivTitulo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Bitmap bmp = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            Bitmap bmpMutado = bmp.copy(Bitmap.Config.ARGB_8888, true);
            ivTitulo.setImageBitmap(bmpMutado);
            Glide.with(getContext())
                    .load(imageByteArray)
                    .asBitmap()
                    .centerCrop()
                    .into(ivTitulo);
        }

        return view;
    }

    @Override
    public void add(@Nullable Object object) {

        if(object instanceof DataTransfered){
            DataTransfered dataTransfered=(DataTransfered)object;
            listDataTransfered.add(dataTransfered);
        }
    }
}
