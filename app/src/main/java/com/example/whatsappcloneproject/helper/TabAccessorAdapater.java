package com.example.whatsappcloneproject.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.whatsappcloneproject.Fragements.ChatFragement;
import com.example.whatsappcloneproject.Fragements.RequestFragement;
import com.example.whatsappcloneproject.Fragements.StatusFragment;

public class TabAccessorAdapater extends FragmentPagerAdapter {

    public TabAccessorAdapater(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position)
        {
            case 0:
                ChatFragement chatFragement= new ChatFragement();
                return chatFragement;
            case 1:
                StatusFragment statusFragment= new StatusFragment();
                return statusFragment;
            case 2:
                RequestFragement requestFragement= new RequestFragement();
                return  requestFragement;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
       switch (position)
       {
           case 0:
               return "Chats";
           case 1:
               return "Status";
           case 2:
               return "Requests";
           default:
               return null;
       }
    }
}
