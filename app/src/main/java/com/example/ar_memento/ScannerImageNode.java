package com.example.ar_memento;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"AndroidApiChecker"})
public class ScannerImageNode extends AnchorNode {
    // We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private static CompletableFuture<ModelRenderable> laptop;
    private static CompletableFuture<ModelRenderable> whiteboard;
    private static CompletableFuture<ModelRenderable> tree;
    private static final String TAG = "ScannerImageNode";

    public ScannerImageNode(Context context) {
        // TODO: This will have to be dynamic, to display certain AR objects.
        // Upon construction, start loading the model.
        if (laptop== null) {
            laptop= ModelRenderable.builder()
                    .setSource(context, Uri.parse("Laptop_01.sfb"))
                    .build();
        }
        if (whiteboard== null) {
            whiteboard= ModelRenderable.builder()
                    .setSource(context, Uri.parse("whiteboard.sfb"))
                    .build();
        }
        if (tree== null) {
            tree= ModelRenderable.builder()
                    .setSource(context, Uri.parse("tree01.sfb"))
                    .build();
        }
    }

    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image, ArFragment arFragment, ViewRenderable scannerInfoCard_Vr, Activity act) {
        // IT IS VERY IMPORTANT THAT THE MODELS ARE FIRST LOADED!
        // TODO: This will have to be dynamic, to display certain AR objects.
        if (!laptop.isDone() || !whiteboard.isDone() || !tree.isDone()) {
            String text = "Laptop Loading...";
            SnackbarHelper.getInstance().showMessage(act, text);
            CompletableFuture.allOf(laptop, whiteboard, tree)
                    .thenAccept((Void aVoid) -> setImage(image, arFragment, scannerInfoCard_Vr, act))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }
        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));

        TransformableNode tNode = new TransformableNode(arFragment.getTransformationSystem());
        tNode.setParent(this);
        // TODO: This will have to be dynamic, to display certain AR objects.
        if (image.getIndex() == 0) {
            tNode.setRenderable(whiteboard.getNow(null));
        } else if (image.getIndex() == 1) {
            tNode.setRenderable(tree.getNow(null));
        }
//        tNode.setRenderable(laptop.getNow(null));


        Node infoCard = new Node();
        infoCard.setParent(tNode);
        infoCard.setEnabled(false);
        infoCard.setRenderable(scannerInfoCard_Vr);
        TextView textView = (TextView) scannerInfoCard_Vr.getView();

        String objectText = "test";
        textView.setText(objectText);
        infoCard.setLocalPosition(new Vector3(0.0f, 0.25f, 0.0f));
        tNode.setOnTapListener(
                (hitTestResult, motionEvent) -> infoCard.setEnabled(!infoCard.isEnabled()));
    }
}
