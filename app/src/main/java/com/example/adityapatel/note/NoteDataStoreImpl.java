package com.example.adityapatel.note;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.example.adityapatel.note.NewNote.TAG;




public class NoteDataStoreImpl implements NoteDataStore {

    private static NoteDataStoreImpl sInstance;
    private static List<NoteData> dataList;
    private static List<NoteData> allUserNoteList;
    private static FirebaseAuth mAuth;
    private static FirebaseUser user;
    private static DatabaseReference mDatabase_post;
    private static DatabaseReference mDatabase_get;
    private static DatabaseReference mAllUserDatabaseReference;

    private StorageReference mStorageRef;
    final List<ImageStoreStructure> image_note_store = new ArrayList<>();
    Context mcontext;
//    final List<String> image_path = new ArrayList<>();


    synchronized public static NoteDataStore sharedInstance(Context context) {

        if (sInstance == null) {

            sInstance = new NoteDataStoreImpl(context);
            mAuth = FirebaseAuth.getInstance();
        }

        return sInstance;
    }
    public void clear(){
        mDatabase_post = null;
        mDatabase_get = null;
        user = null;
        sInstance = null;
    }


    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase_post = FirebaseDatabase.getInstance().getReference();
        mDatabase_get = FirebaseDatabase.getInstance().getReference().child("iuser-notes").child(user.getUid());
        mAllUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("inotes");

    }

    private int getNoteIndex(String noteId) {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getNoteId().equals(noteId)) {
                return i;
            }
        }
        return -1;
    }

    private NoteDataStoreImpl(Context context) {
        this.mcontext = context;
        init();
        dataList = new ArrayList<>();
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d( TAG, "onChildAdded:" + dataSnapshot.getKey() );
                NoteData note = dataSnapshot.getValue( NoteData.class );
                dataList.add( note );
                try {
                    downloadImage( note );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Consumer subject : consumers) {
                    subject.consume( dataList );
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d( TAG, "onChildChanged:" + dataSnapshot.getKey() );
                NoteData note = dataSnapshot.getValue( NoteData.class );
                int index = getNoteIndex( note.getNoteId() );
                dataList.remove( index );
                dataList.add( index, note );
                try {
                    downloadImage( note );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private final List<Consumer> consumers = new ArrayList<>();
    private final List<AllUserNoteConsumer> allUserNoteConsumers = new ArrayList<>();
    //private AllUserNoteConsumer<List<NoteData>> allUserNoteConsumer;

    @Override
    public FirebaseUser getUser() {
        return user;
    }

    @Override
    public void logoutUser() {
        mAuth.signOut();
        dataList.clear();
    }

    @Override
    public void setUser(FirebaseUser user1) {
        user = user1;
    }

    @Override
    public void setNoteList(List<NoteData> noteList) {
    }

    @Override
    public void addNote(NoteData noteData) {
        if (noteData.getImageIds().size() == 0) {
            String key = mDatabase_post.child("inotes").push().getKey();
            noteData.setNoteId(key);
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/inotes/" + key, noteData);
            childUpdates.put("/iuser-notes/" + noteData.getUserId() + "/" + key, noteData);
            mDatabase_post.updateChildren(childUpdates);
            //dataList.add(noteData);
        } else {
            String key = mDatabase_post.child("inotes").push().getKey();
            noteData.setNoteId(key);
            //noteData.setImageId();
            for (int i = 0; i < noteData.getImagePaths().size(); i++) {
                String id = noteData.getImageIds().get(i) + key;
                noteData.setImageId(id, i);
            }
            //NoteData tempNote = noteData;
            //tempNote.setImagePaths(null);
            uploadImage(noteData);
            //noteData.setImage(null);
            //noteData.setImagePaths(null);
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/inotes/" + key, noteData);
            childUpdates.put("/iuser-notes/" + noteData.getUserId() + "/" + key, noteData);
            mDatabase_post.updateChildren(childUpdates);
            //noteData.setImagePaths(imagepaths);
            // dataList.add(noteData);
        }
    }


    @Override
    public void updateNote(int oldNoteToBeUpdated, NoteData noteData, boolean imageFlag) {
        if (imageFlag) {
            String key = noteData.getNoteId();
            for (int i = 0; i < noteData.getImagePaths().size(); i++) {
                noteData.setImageId(key + Integer.toString(i), i);
            }
            uploadImage(noteData);
        }

        mDatabase_post = FirebaseDatabase.getInstance().getReference();
        mDatabase_post.child("inotes").child(noteData.getNoteId()).setValue(noteData);
        mDatabase_post.child("iuser-notes").child(noteData.getUserId()).child(noteData.getNoteId()).setValue(noteData);
        /*Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/inotes/" + noteData.getNoteId(), noteData);
        childUpdates.put("/iuser-notes/" + noteData.getUserId() + "/" + noteData.getNoteId(), noteData);
        mDatabase_post.updateChildren(childUpdates);*/
        dataList.remove(oldNoteToBeUpdated);
        dataList.add(oldNoteToBeUpdated, noteData);
        for (Consumer subject : consumers) {
            subject.consume(dataList);
        }
    }

    @Override
    public void deleteNote(int index) {
        NoteData noteData = dataList.get(index);
        mDatabase_post = FirebaseDatabase.getInstance().getReference();
        mDatabase_post.child("inotes").child(noteData.getNoteId()).setValue(null);
        mDatabase_post.child("iuser-notes").child(noteData.getUserId()).child(noteData.getNoteId()).setValue(null);
        /*Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/inotes/" + noteData.getNoteId(), null);
        childUpdates.put("/iuser-notes/" + noteData.getUserId() + "/" + noteData.getNoteId(), null);
        mDatabase_post.updateChildren(childUpdates);*/
        dataList.remove(index);
        for (Consumer subject : consumers) {
            subject.consume(dataList);
        }
    }

    @Override
    public List<NoteData> getNotes() {
        return dataList;
    }

    @Override
    public <T> void registerSubject(Consumer<T> consumer) {
        consumers.add(consumer);

    }

    @Override
    public <T> void registerAllUserNoteSubject(AllUserNoteConsumer<T> allUserNoteConsumer) {
        allUserNoteConsumers.add(allUserNoteConsumer);
    }

    @Override
    public void load_notes() {
        mDatabase_get.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataList.clear();

                int count = 0;
                int imageCount = 0;
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    NoteData note = noteDataSnapshot.getValue(NoteData.class);
                    dataList.add(note);

                    if (note.getImageIds().size() != 0) {
                        int isImageAlreadyStore = searchImageId(note.getNoteId());
                        if (isImageAlreadyStore != -1) {
                            try {
                                downloadImage(note);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ImageStoreStructure imageStoreStructure = new ImageStoreStructure(count, note.getNoteId(), null);
                            image_note_store.add(imageStoreStructure);
                            //image_note_id.add(note.getImageId());
                            try {
                                downloadImage(note);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                for (Consumer subject : consumers) {
                    subject.consume(dataList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void loadAllUserNotes() {
        mAllUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUserNoteList = new ArrayList<>();

                int count = 0;
                int imageCount = 0;
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    NoteData note = noteDataSnapshot.getValue(NoteData.class);
                    allUserNoteList.add(note);

                    if (note.getImageIds().size() != 0) {
                        int isImageAlreadyStore = searchImageId(note.getNoteId());
                        if (isImageAlreadyStore != -1) {
                            try {
                                downloadImage(note);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ImageStoreStructure imageStoreStructure = new ImageStoreStructure(count, note.getNoteId(), null);
                            image_note_store.add(imageStoreStructure);
                            //image_note_id.add(note.getImageId());
                            try {
                                downloadImage(note);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                for (AllUserNoteConsumer subject : allUserNoteConsumers) {
                    subject.consume(allUserNoteList);
                }
               // AllUserNoteConsumer.consume(allUserNoteList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadImage(final NoteData noteData) {
        String noteId = noteData.getNoteId();
        for (int i = 0; i < noteData.getImagePaths().size(); i++) {
            final Bitmap bitmap = BitmapFactory.decodeFile(noteData.getImagePaths().get(i));
            mStorageRef = FirebaseStorage.getInstance().getReference().child("inote-images").child(noteData.getNoteId()).child(noteData.getImageIds().get(i) + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();
            final boolean[] flag = {false};
            UploadTask uploadTask = mStorageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    int i = 0;
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc
                    flag[0] = true;

                }
            });

        }
    }

        private void downloadImage ( final NoteData noteData) throws IOException {
            if (noteData.getImageIds() == null) {

            } else {
                for (int i = 0; i < noteData.getImageIds().size(); i++) {
                    mStorageRef = FirebaseStorage.getInstance().getReference().child("inote-images").child(noteData.getNoteId()).child(noteData.getImageIds().get(i) + ".jpg");

                    ContextWrapper cw = new ContextWrapper(mcontext);
                    final File directory = cw.getDir(noteData.getNoteId(), Context.MODE_PRIVATE);
                    final File myImageFile = new File(directory, noteData.getImageIds().get(i) + ".jpg"); // Create image file
                    if (myImageFile.exists()) {
                        String path = Long.toString(myImageFile.getTotalSpace());
                    } else {
                        //final File localFile = File.createTempFile(noteData.getImageIds().get(i), "jpg");
                        mStorageRef.getFile(myImageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                String path = myImageFile.getAbsolutePath();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }
                }
            }

        }

        private int searchImageId (String noteId){
            for (int i = 0; i < image_note_store.size(); i++) {
                if (image_note_store.get(i).getNoteID().equals(noteId)) {
                    return i;
                }
            }

            return -1;
        }
}


