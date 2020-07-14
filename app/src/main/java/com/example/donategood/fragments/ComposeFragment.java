package com.example.donategood.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.donategood.Camera;
import com.example.donategood.Query;
import com.example.donategood.R;
import com.example.donategood.models.Charity;
import com.example.donategood.models.Offering;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    public static final Integer CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 10;
    public static final Integer UPLOAD_PHOTO_CODE = 20;

    private Query query;
    private Camera camera;

    private EditText etTitle;
    private EditText etPrice;
    private Button btnTakePhoto;
    private Button btnUploadPhoto;
    private ImageView ivPhoto;
    private EditText etTags;
    private Button btnSubmit;
    private ProgressBar pb;

    private String title;
    private String price;
    private ArrayList<String> tags;
    private String charity;
    private Spinner spinner;
    private File photoFile;
    public String photoFileName = "photo.jpg";

    public ComposeFragment() {
        //required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitle = view.findViewById(R.id.etTitle);
        etPrice = view.findViewById(R.id.etPrice);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        ivPhoto = view.findViewById(R.id.ivComposePhoto);
        etTags = view.findViewById(R.id.etTags);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        pb = (ProgressBar) view.findViewById(R.id.pbLoading);

        query = new Query();
        camera = new Camera();

        spinner = (Spinner) view.findViewById(R.id.spinnerCharity);
        setUpSpinner(view);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btnTakePhoto onClick");
                launchCamera();
            }
        });

        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btnUploadPhoto onClick");
                pickPhoto(view);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btnSubmit onClick");
                title = etTitle.getText().toString();
                price = etPrice.getText().toString();
                tags = editTags(etTags.getText().toString());

                if (title.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                } else if (price.isEmpty()) {
                    Toast.makeText(getContext(), "Price cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                savePost();
            }
        });
    }

    private ArrayList<String> editTags(String tags) {
        String[] tagArray = tags.split(", ");
        ArrayList<String> tagList = new ArrayList<>();
        tagList.addAll(Arrays.asList(tagArray));
        Log.i(TAG, "TAGS: " + tagList.toString());
        return tagList;
    }

    private void savePost() {
        pb.setVisibility(ProgressBar.VISIBLE);
        query.queryCharityByName(charity, new FindCallback<Charity>() {
            @Override
            public void done(List<Charity> charities, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Successfully got charity");

                final Offering offering = new Offering();
                offering.setTitle(title);
                offering.setImage(new ParseFile(photoFile));
                offering.setPrice(Integer.valueOf(price));
                offering.setCharity(charities.get(0));
                offering.setTags(tags);
                offering.setUser(ParseUser.getCurrentUser());
                offering.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving", e);
                            Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                        }
                        Log.i(TAG, "Post save was successful!");
                        etTitle.setText("");
                        etPrice.setText("");
                        etTags.setText("");
                        ivPhoto.setImageResource(0);
                        pb.setVisibility(ProgressBar.INVISIBLE);

                        //go to detail fragment
                        final FragmentManager fragmentManager = ((AppCompatActivity)getContext()).getSupportFragmentManager();
                        Fragment fragment = DetailFragment.newInstance(offering.getObjectId());
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                    }
                });
            }
        });
    }

    protected void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = camera.getPhotoFileUri(photoFileName, getContext());

        // wrap File object into a content provider; required for API >= 24
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.donateGood", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public void pickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, UPLOAD_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Load the taken image into a preview
                ivPhoto.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if ((data != null) && requestCode == UPLOAD_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = camera.loadFromUri(photoUri, getContext());

            // Load the selected image into a preview
            ivPhoto.setImageBitmap(selectedImage);
            photoFile = camera.createFile(getContext(), selectedImage);
        }
    }

    private void setUpSpinner(View view) {
        final List<String> charitiesNames = new ArrayList<>();
        query.queryAllCharities(new FindCallback<Charity>() {
            @Override
            public void done(List<Charity> charities, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting charities", e);
                }
                Log.i(TAG, "Successfully got charities");
                //populate charitiesNames with all names of charities in database
                for (Charity charity : charities) {
                    charitiesNames.add(charity.getTitle());
                }

                // Create an ArrayAdapter for spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, charitiesNames);

                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                        charity = (String) adapterView.getItemAtPosition(pos);
                        Log.i(TAG, "onItemSelected with charity: " + charity);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        Log.i(TAG, "onNothingSelected");
                    }
                });
            }
        });
    }
}