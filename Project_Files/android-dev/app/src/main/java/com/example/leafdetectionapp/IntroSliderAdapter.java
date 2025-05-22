//package com.example.leafdetectionapp;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.viewpager.widget.PagerAdapter;
//
//public class IntroSliderAdapter extends PagerAdapter {
//
//    Context context;
//
//    int images[] = {
//            R.drawable.logo,
////            R.drawable.image2,
////            R.drawable.leaf4,
////            R.drawable.image4
//    };
//
//    int headings[] = {
//
//            R.string.heading_one,
////            R.string.heading_three,
////            R.string.heading_two,
////            R.string.heading_fourth
//    };
//
//    int description[] = {
//
////            R.string.desc_one,
////            R.string.desc_two,
////            R.string.desc_three,
////            R.string.desc_fourth
//    };
//
//    public IntroSliderAdapter(Context context){
//
//        this.context = context;
//
//    }
//
//    @Override
//    public int getCount() {
//        return  headings.length;
//    }
//
//    @Override
//    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
//        return view == (LinearLayout) object;
//    }
//
//    @NonNull
//    @Override
//    public Object instantiateItem(@NonNull ViewGroup container, int position) {
//
//        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
//
//        View view = layoutInflater.inflate(R.layout.slider_layout,container,false);
//
//        ImageView slidetitleimage = view.findViewById(R.id.titleImage);
//        TextView slideHeading = view.findViewById(R.id.texttitle);
////        TextView slideDesciption = view.findViewById(R.id.textdeccription);
//
//        slidetitleimage.setImageResource(images[position]);
//        slideHeading.setText(headings[position]);
////        slideDesciption.setText(description[position]);
//
//        container.addView(view);
//
//        return view;
//
//    }
//
//    @Override
//    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//
//        container.removeView((LinearLayout)object);
//
//    }
//}
