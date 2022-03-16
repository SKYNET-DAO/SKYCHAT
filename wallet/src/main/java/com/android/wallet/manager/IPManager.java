package com.android.wallet.manager;

import android.content.Context;

import com.android.base.BaseApp;
import com.android.wallet.R;
import com.android.wallet.constants.Constants;
import com.android.wallet.utils.IpV4Util;

import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class IPManager {


        public static List<PeerAddress> initIp(){
            //设置ip
                try {
                   List<PeerAddress> peerAddresses=new ArrayList<>();
                    String[] Ips;
                    if(Constants.NETWORK_PARAMETERS instanceof TestNet3Params){
                        Ips=BaseApp.getContext().getResources().getStringArray(R.array.test_ips);
                    }else{
                        Ips=BaseApp.getContext().getResources().getStringArray(R.array.main_ips);
                    }
                   for(String ip:Arrays.asList(Ips)){
                       byte[] addr =  IpV4Util.ipToBytes(ip);
                       PeerAddress temp=  new PeerAddress(Constants.NETWORK_PARAMETERS, InetAddress.getByAddress(addr), Constants.NETWORK_PARAMETERS.getPort());
                       peerAddresses.add(temp);

                   }
                   return peerAddresses;

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                return null;

        }





}
