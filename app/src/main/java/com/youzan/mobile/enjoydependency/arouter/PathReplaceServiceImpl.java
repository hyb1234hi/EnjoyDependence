package com.youzan.mobile.enjoydependency.arouter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.PathReplaceService;

@Route(path = "/app/pathReplace")
public class PathReplaceServiceImpl implements PathReplaceService {

    @Override
    public String forString(String path) {
        return path;
    }

    @Override
    public Uri forUri(Uri uri) {
        Log.e("hello", uri.getPath());
        return uri;
    }

    @Override
    public void init(Context context) {

    }
}
