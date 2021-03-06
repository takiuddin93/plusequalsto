package plusequalsto.com.plusequalstoapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Model> list;
    private RecyclerViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String baseURL = "http://www.plusequalsto.com";
    public static List<AllPosts> mAllPost;
    int cacheSize = 100 * 1024 * 1024; // 100 MiB

    Retrofit retrofit = null;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = (RecyclerView) homeView.findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) homeView.findViewById(R.id.progressbar);
        swipeRefreshLayout = (SwipeRefreshLayout) homeView.findViewById(R.id.swiperefresh);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        list = new ArrayList<Model>();
        getRetrofit();
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // call getRetrofit
                        getRetrofit();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
        adapter = new RecyclerViewAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return homeView;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getRetrofit() {
        Cache cache = new Cache(getActivity().getCacheDir(), cacheSize);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Interceptor.Chain chain)
                            throws IOException {
                        Request request = chain.request();
                        if (!isNetworkAvailable()) {
                            int maxStale = 60 * 60 * 24; // tolerate one day stale
                            request = request
                                    .newBuilder()
                                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                                    .build();
                        }
                        return chain.proceed(request);
                    }
                })
                .build();

        Log.e("HomeFragment", "HomeFragment0 " + retrofit);

        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Log.e("HomeFragment", "HomeFragment1 " + retrofit);

            RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
            Call<List<AllPosts>> call = service.getAllPostsInfo();

            Log.e("HomeFragment", "HomeFragment2 " + call);

            // to make call to dynamic URL
            // String yourURL = yourURL.replace(BaseURL,"");
            // Call<List<AllPosts>>  call = service.getAllPostsInfo( yourURL);
            /// to get only 6 post from your blog
            // http://www.plusequalsto.com/wp-json/wp/v2/posts?per_page=2
            // to get any specific blog post, use id of post
            //  http://www.plusequalsto.com/wp-json/wp/v2/posts/1179
            // to get only title and id of specific
            // http://www.plusequalsto.com/android/wp-json/wp/v2/posts/1179?fields=id,title
            call.enqueue(new Callback<List<AllPosts>>() {
                @Override
                public void onResponse(Call<List<AllPosts>> call, Response<List<AllPosts>> response) {
                    mAllPost = response.body();

                    Log.e("HomeFragment", "HomeFragment3 " + mAllPost);

                    progressBar.setVisibility(View.GONE);
                    for (int i = 0; i < mAllPost.size(); i++) {
                        Log.e("HomeFragment", " title " + response.body().get(i).getTitle().getRendered() + " " +
                                response.body().get(i).getId());
                        String author = response.body().get(i).getAuthor().toString();
                        String date = response.body().get(i).getDate();
                        String categories = response.body().get(i).getCategories().toString();
                        list.add(new Model(Model.IMAGE_TYPE, response.body().get(i).getTitle().getRendered(),
                                author, date, categories, response.body().get(i).getLinks().getWpFeaturedmedia().get(0).getHref()));
                        Log.e("HomeFragment", " response " + response.body().get(i).getJetpackFeaturedMediaUrl());
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<AllPosts>> call, Throwable t) {

                    Log.e("HomeFragment", "HomeFragment4 " + t);

                }
            });
        } else {

            Log.e("HomeFragment", "HomeFragment1 " + retrofit);

            RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
            Call<List<AllPosts>> call = service.getAllPostsInfo();

            Log.e("HomeFragment", "HomeFragment2 " + call);

            // to make call to dynamic URL
            // String yourURL = yourURL.replace(BaseURL,"");
            // Call<List<AllPosts>>  call = service.getAllPostsInfo( yourURL);
            /// to get only 6 post from your blog
            // http://www.plusequalsto.com/wp-json/wp/v2/posts?per_page=2
            // to get any specific blog post, use id of post
            //  http://www.plusequalsto.com/wp-json/wp/v2/posts/1179
            // to get only title and id of specific
            // http://www.plusequalsto.com/android/wp-json/wp/v2/posts/1179?fields=id,title
            call.enqueue(new Callback<List<AllPosts>>() {
                @Override
                public void onResponse(Call<List<AllPosts>> call, Response<List<AllPosts>> response) {
                    mAllPost = response.body();

                    Log.e("HomeFragment", "HomeFragment3 " + mAllPost);

                    progressBar.setVisibility(View.GONE);
                    for (int i = 0; i < mAllPost.size(); i++) {
                        Log.e("HomeFragment", " title " + response.body().get(i).getTitle().getRendered() + " " +
                                response.body().get(i).getId());
                        String author = response.body().get(i).getAuthor().toString();
                        String date = response.body().get(i).getDate();
                        String categories = response.body().get(i).getCategories().toString();
                        list.add(new Model(Model.IMAGE_TYPE, response.body().get(i).getTitle().getRendered(),
                                author, date, categories, response.body().get(i).getLinks().getWpFeaturedmedia().get(0).getHref()));
                        Log.e("HomeFragment", " response " + response.body().get(i).getJetpackFeaturedMediaUrl());
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<AllPosts>> call, Throwable t) {

                    Log.e("HomeFragment", "HomeFragment4 " + t);

                }
            });
        }
    }

    public static List<AllPosts> getList() {
        return mAllPost;
    }
}
