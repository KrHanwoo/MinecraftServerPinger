import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class MinecraftServerPinger {
    private final String address;
    private final int port;
    private String json;
    private String description;
    private Integer online;
    private Integer max;
    private Map<String, UUID> samples;
    private String version;
    private Integer protocol;
    private String favicon;

    public MinecraftServerPinger(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void connect() throws Exception {
        InetSocketAddress host = new InetSocketAddress(address, port);
        Socket socket = new Socket();
        socket.connect(host, 2000);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        byte[] handshakeMessage = createHandshakeMessage(address, port);
        writeVarInt(output, handshakeMessage.length);
        output.write(handshakeMessage);
        output.writeByte(0x01);
        output.writeByte(0x00);
        readVarInt(input);
        int packetId = readVarInt(input);
        if (packetId != 0) throw new IOException();
        int length = readVarInt(input);
        if (length < 1) throw new IOException();
        byte[] in = new byte[length];
        input.readFully(in);
        json = new String(in);
        parseJson();
    }

    private void parseJson() {
        JSONObject jo = new JSONObject(json);
        description = String.valueOf(jo.optJSONObject("description"));
        if (description == null) description = jo.optString("description", null);
        favicon = jo.optString("favicon", null);
        if (jo.optJSONObject("players", new JSONObject()).has("online"))
            online = jo.optJSONObject("players").getInt("online");
        if (jo.optJSONObject("players", new JSONObject()).has("max")) max = jo.optJSONObject("players").getInt("max");
        JSONArray array = jo.optJSONObject("players", new JSONObject()).optJSONArray("sample");
        if (array != null) {
            samples = new HashMap<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                if (object == null) continue;
                String name = object.optString("name", null);
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(object.optString("id"));
                } catch (Exception ignored) {
                }
                samples.put(name, uuid);
            }
        }
        version = jo.optJSONObject("version", new JSONObject()).optString("name", null);
        if (jo.optJSONObject("version", new JSONObject()).has("protocol"))
            protocol = jo.optJSONObject("version").getInt("protocol");
    }

    private byte[] createHandshakeMessage(String host, int port) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(buffer);
        handshake.writeByte(0x00);
        writeVarInt(handshake, 0);
        writeString(handshake, host, StandardCharsets.UTF_8);
        handshake.writeShort(port);
        writeVarInt(handshake, 1);
        return buffer.toByteArray();
    }

    private void writeString(DataOutputStream out, String string, Charset charset) throws Exception {
        byte[] bytes = string.getBytes(charset);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private void writeVarInt(DataOutputStream out, int paramInt) throws Exception {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    private int readVarInt(DataInputStream in) throws Exception {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new IllegalArgumentException();
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getJson() {
        return json;
    }

    public String getDescription() {
        return description;
    }

    public Integer getOnline() {
        return online;
    }

    public Integer getMax() {
        return max;
    }

    public Map<String, UUID> getSamples() {
        return samples;
    }

    public String getVersion() {
        return version;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public String getFavicon() {
        return favicon;
    }
}
