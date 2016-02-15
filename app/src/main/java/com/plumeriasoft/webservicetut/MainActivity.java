package com.plumeriasoft.webservicetut;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.plumeriasoft.webservicetut.models.MovieModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BufferedReader br;
    InputStream stream;
    ListView lv;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.listView);
        // Create default options which will be used for every
//  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config); // Do it on Application start

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading,please wait...");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Refresh");
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                new JSONTask().execute("http://jsonparsing.parseapp.com/jsonData/moviesData.txt");
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    class JSONTask extends AsyncTask<String, String, List<MovieModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected List<MovieModel> doInBackground(String... params) {

            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                stream = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(stream));

                StringBuffer sb = new StringBuffer();
                String line = "";

                while ((line = br.readLine()) != null) {

                    sb.append(line);
                }

                String finalJson = sb.toString();

                JSONObject parentobject = new JSONObject(finalJson);
                JSONArray parentArray = parentobject.getJSONArray("movies");


                List<MovieModel> moviemodel = new ArrayList<>();
                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    MovieModel model = new MovieModel();
                    model.setMovie(finalObject.getString("movie"));
                    model.setYear(finalObject.getInt("year"));
                    model.setRating((float) finalObject.getDouble("rating"));
                    model.setDuration(finalObject.getString("duration"));
                    model.setDirector(finalObject.getString("director"));
                    model.setTagline(finalObject.getString("tagline"));
                    model.setImage(finalObject.getString("image"));
                    model.setStory(finalObject.getString("story"));

                    List<MovieModel.Cast> castList = new ArrayList<>();
                    for (int j = 0; j < finalObject.getJSONArray("cast").length(); j++) {
                        MovieModel.Cast cast = new MovieModel.Cast();
                        JSONObject castObject = finalObject.getJSONArray("cast").getJSONObject(j);
                        cast.setName(castObject.getString("name"));
                        castList.add(cast);
                    }

                    model.setCastList(castList);
                    moviemodel.add(model);
                }
                return moviemodel;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

                if (connection != null)
                    connection.disconnect();
                try {
                    if (br != null)
                        br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<MovieModel> s) {
            super.onPostExecute(s);
            dialog.dismiss();
            MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.row, s);
            lv.setAdapter(adapter);

        }
    }

    public class MovieAdapter extends ArrayAdapter {
        private List<MovieModel> movieModelList;
        private int resource;
        private LayoutInflater inflater;

        public MovieAdapter(Context context, int resource, List<MovieModel> objects) {
            super(context, resource, objects);
            movieModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        //Thi is
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.row, null);
                holder.movieIcon = (ImageView) convertView.findViewById(R.id.imageView);
                holder.movieName = (TextView) convertView.findViewById(R.id.MovieName);
                holder.tvYear = (TextView) convertView.findViewById(R.id.tvYear);
                holder.tvTagline = (TextView) convertView.findViewById(R.id.tvTagline);
                holder.tvDirector = (TextView) convertView.findViewById(R.id.tvDirector);
                holder.tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
                holder.tvCast = (TextView) convertView.findViewById(R.id.tvCast);
                holder.tvStory = (TextView) convertView.findViewById(R.id.tvStory);
                holder.ratingMovie = (RatingBar) convertView.findViewById(R.id.ratingBar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.movieName.setText(movieModelList.get(position).getMovie());
            holder.tvYear.setText("Year:" + movieModelList.get(position).getYear());
            holder.tvTagline.setText(movieModelList.get(position).getTagline());
            holder.tvDuration.setText(movieModelList.get(position).getDuration());
            holder.tvDirector.setText(movieModelList.get(position).getDirector());
            holder.tvStory.setText(movieModelList.get(position).getStory());
            holder.ratingMovie.setRating(movieModelList.get(position).getRating() / 2);

            final ProgressBar progressBar;
            progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            ImageLoader.getInstance().displayImage(movieModelList.get(position).getImage(), holder.movieIcon, new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            StringBuffer buffer = new StringBuffer();
            for (MovieModel.Cast cast : movieModelList.get(position).getCastList()) {
                buffer.append(cast.getName() + ", ");
            }
            holder.tvCast.setText(buffer.toString());


            return convertView;
        }

        class ViewHolder {
            private ImageView movieIcon;
            private TextView movieName;
            private TextView tvYear;
            private TextView tvTagline;
            private TextView tvDirector;
            private TextView tvDuration;
            private RatingBar ratingMovie;
            private TextView tvCast;
            private TextView tvStory;
        }
    }

}