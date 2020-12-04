package li.cil.oc2.serialization.serializers;

import com.google.gson.*;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.common.bus.RPCAdapter;

import java.lang.reflect.Type;

public final class RPCDeviceWithIdentifierJsonSerializer implements JsonSerializer<RPCAdapter.RPCDeviceWithIdentifier> {
    @Override
    public JsonElement serialize(final RPCAdapter.RPCDeviceWithIdentifier src, final Type typeOfSrc, final JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject deviceJson = new JsonObject();
        deviceJson.add("deviceId", context.serialize(src.identifier));
        deviceJson.add("typeNames", context.serialize(src.device.getTypeNames()));

        final JsonArray methodsJson = new JsonArray();
        deviceJson.add("methods", methodsJson);
        for (final RPCMethod method : src.device.getMethods()) {
            methodsJson.add(context.serialize(method, RPCMethod.class));
        }

        return deviceJson;
    }
}
