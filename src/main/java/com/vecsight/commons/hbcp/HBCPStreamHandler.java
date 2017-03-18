package com.vecsight.commons.hbcp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class HBCPStreamHandler {

    private final static Charset charset = StandardCharsets.US_ASCII;

    private InputStream inputStream;
    private OutputStream outputStream;

    private BufferedReader bufferedReader;

    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();

    public HBCPStreamHandler(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    public Map<String, HBCPValue> nextMsg() throws IOException, ValueTypeMismatchException {
        String tmpline;
        HashMap<String, HBCPValue> map = new HashMap<>();
        while ((tmpline = bufferedReader.readLine()) != null && tmpline.length() > 0) {
            if (tmpline.contains(":")) {
                String[] kv = tmpline.split(":", 2);
                if (kv[0].contains(",")) {
                    String[] kt = kv[0].split(",", 2);
                    if (kt[1].equals("b64")) {
                        map.put(kt[0], new HBCPValue(decoder.decode(kv[1])));
                    } else {
                        throw new ValueTypeMismatchException("Unknown meta: " + kt[1]);
                    }
                } else {
                    map.put(kv[0], new HBCPValue(kv[1]));
                }
            } else {
                map.put(tmpline, null);
            }
        }
        if (tmpline == null) throw new EOFException();
        return map;
    }

    public void sendMsg(Map<String, HBCPValue> map) throws ValueTypeMismatchException, IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, HBCPValue> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getValue().getValueType() == HBCPValueType.STRING) {
                    sb.append(entry.getKey()).append(":").append(entry.getValue().getString()).append("\n");
                } else if (entry.getValue().getValueType() == HBCPValueType.BYTES) {
                    sb.append(entry.getKey()).append(",b64:").append(encoder.encodeToString(entry.getValue().getBytes())).append("\n");
                }
            } else {
                sb.append(entry.getKey()).append("\n");
            }
        }
        sb.append("\n");
        outputStream.write(sb.toString().getBytes(charset));
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9900);

            Thread clientThread = new Thread(() -> {
                try {
                    Socket socket = new Socket("localhost", 9900);
                    HBCPStreamHandler handler = new HBCPStreamHandler(socket.getInputStream(), socket.getOutputStream());
                    Map<String, HBCPValue> map = new HashMap<>();
                    map.put("test1", new HBCPValue("just a string!"));
                    map.put("test2", new HBCPValue(new byte[]{2, 3, 3}));
                    map.put("no value test3", null);
                    handler.sendMsg(map);
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            clientThread.start();

            Socket socket = serverSocket.accept();

            HBCPStreamHandler hbcpStreamHandler = new HBCPStreamHandler(socket.getInputStream(), socket.getOutputStream());
            while (true) {
                System.out.println(hbcpStreamHandler.nextMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
