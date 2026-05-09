package j.core.web.mcp;

import tools.jackson.databind.ObjectMapper;

public class McpJson {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     *
     * @return
     */
    public static ObjectMapper mapper(){
        return  MAPPER;
    }
}
