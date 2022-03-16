package com.android.wallet.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.TypefaceSpan;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import com.android.wallet.constants.Constants;
import com.google.common.base.Stopwatch;
import com.orhanobut.logger.Logger;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.VersionMessage;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletProtobufSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WalletUtils {

    public static boolean isEntirelySelf(final Transaction tx, final Wallet wallet) {
        for (final TransactionInput input : tx.getInputs()) {
            final TransactionOutput connectedOutput = input.getConnectedOutput();
            if (connectedOutput == null || !connectedOutput.isMine(wallet))
                return false;
        }

        for (final TransactionOutput output : tx.getOutputs()) {
            if (!output.isMine(wallet))
                return false;
        }

        return true;
    }



    @Nullable
    public static Address getToAddressOfSent(final Transaction tx, final Wallet wallet) {
        for (final TransactionOutput output : tx.getOutputs()) {
            try {
                if (!output.isMine(wallet)) {
                    final Script script = output.getScriptPubKey();
                    return script.getToAddress(Constants.NETWORK_PARAMETERS, true);
                }
            } catch (final ScriptException x) {
                // swallow
            }
        }

        return null;
    }


    @Nullable
    public static Address getWalletAddressOfReceived(final Transaction tx, final Wallet wallet) {
        for (final TransactionOutput output : tx.getOutputs()) {
            try {
                if (output.isMine(wallet)) {
                    final Script script = output.getScriptPubKey();
                    return script.getToAddress(Constants.NETWORK_PARAMETERS, true);
                }
            } catch (final ScriptException x) {
                // swallow
            }
        }

        return null;
    }


    public static Spanned formatAddress(final Address address, final int groupSize, final int lineSize) {
        return formatHash(address.toString(), groupSize, lineSize);
    }

    public static Spanned formatHash(final String hash, final int groupSize, final int lineSize) {
        return formatHash(null, hash, groupSize, lineSize, Constants.CHAR_THIN_SPACE);
    }

    public static Spanned formatHash(@Nullable final String prefix, final String hash, final int groupSize,
                                     final int lineSize, final char groupSeparator) {
        final SpannableStringBuilder builder = prefix != null ? new SpannableStringBuilder(prefix)
                : new SpannableStringBuilder();

        final int len = hash.length();
        for (int i = 0; i < len; i += groupSize) {
            final int end = i + groupSize;
            final String part = hash.substring(i, end < len ? end : len);

            builder.append(part);
            builder.setSpan(new MonospaceSpan(), builder.length() - part.length(), builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < len) {
                final boolean endOfLine = lineSize > 0 && end % lineSize == 0;
                builder.append(endOfLine ? '\n' : groupSeparator);
            }
        }

        return SpannedString.valueOf(builder);
    }


    private static class MonospaceSpan extends TypefaceSpan {
        public MonospaceSpan() {
            super("monospace");
        }

        // TypefaceSpan doesn't implement this, and we need it so that Spanned.equals() works.
        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (o == null || o.getClass() != getClass())
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }


    public static int maxConnectedPeers(Context context) {
        ActivityManager   activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getMemoryClass() <= 128 ? 4 : 6;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static int scryptIterationsTarget(Context context) {
        ActivityManager   activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        return activityManager.getMemoryClass() <= 128 || Build.SUPPORTED_64_BIT_ABIS.length == 0
                ? Constants.SCRYPT_ITERATIONS_TARGET_LOWRAM : Constants.SCRYPT_ITERATIONS_TARGET;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void autoBackupWallet(final Context context, final Wallet wallet) {
        final Stopwatch watch = Stopwatch.createStarted();
        final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(wallet).toBuilder();

        // strip redundant
        builder.clearTransaction();
        builder.clearLastSeenBlockHash();
        builder.setLastSeenBlockHeight(-1);
        builder.clearLastSeenBlockTimeSecs();
        final Protos.Wallet walletProto = builder.build();

        try (final OutputStream os = context.openFileOutput(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF,
                Context.MODE_PRIVATE)) {
            walletProto.writeTo(os);
            watch.stop();
            Logger.i("wallet backed up to: '{}', took {}", Constants.Files.WALLET_KEY_BACKUP_PROTOBUF, watch);
        } catch (final IOException x) {
            Logger.e("problem writing wallet backup", x);
        }
    }


    public static String httpUserAgent(final Context context) {
        final VersionMessage versionMessage = new VersionMessage(Constants.NETWORK_PARAMETERS, 0);
        versionMessage.appendToSubVer(Constants.USER_AGENT, packageInfo(context).versionName, null);
        return versionMessage.subVer;
    }

    private static PackageInfo packageInfo;

    public static synchronized PackageInfo packageInfo(Context context) {
        // replace by BuildConfig.VERSION_* as soon as it's possible
        if (packageInfo == null) {
            try {
                packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return packageInfo;
    }


}
