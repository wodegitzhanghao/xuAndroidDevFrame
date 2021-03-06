package com.kd.net.bean;

import java.util.List;

/**
 * Created by xu on 2017/8/29.
 */

public class NewXiaoyaojiBean {


    private String expires;
    private String environments;
    private String description;
    private String permission;
    private String userId;
    private String version;
    private String createTime;
    private String name;
    private String id;
    private String lastUpdateTime;
    private String status;
    private GlobalBean global;


    public GlobalBean getGlobal() {
        return global;
    }

    public void setGlobal(GlobalBean global) {
        this.global = global;
    }

    public List<ChildrenEntity> getDocs() {
        return docs;
    }

    public void setDocs(List<ChildrenEntity> docs) {
        this.docs = docs;
    }

    private List<ChildrenEntity> docs;

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getEnvironments() {
        return environments;
    }

    public void setEnvironments(String environments) {
        this.environments = environments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class GlobalBean {
        /**
         * environment : [{"name":"test","t":1504074915562,"vars":[{"name":"host","value":"1"}]}]
         * http : {"requestHeaders":[{"require":"true","children":[],"name":"t","id":"oqek6d"}],"responseHeaders":[{"require":"true","children":[],"name":"tttt","id":"gigwoq"}],"requestArgs":[{"require":"true","children":[],"type":"string","name":"type","id":"zypirb"}],"responseArgs":[]}
         * id : TjuoAvvvB
         * projectId : Tjuo46VMp
         * status : [{"name":"有效","value":"ENABLE","t":1493901719144},{"name":"废弃","value":"DEPRECATED","t":1493901728060}]
         */

        private String environment;
        private String http;
        private String id;
        private String projectId;
        private String status;

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getHttp() {
            return http;
        }

        public void setHttp(String http) {
            this.http = http;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ChildrenEntity {
        /**
         * children : []
         * content : {"requestHeaders":[],"requestMethod":"POST","dataType":"FORM-DATA","requestArgs":[{"children":[],"name":"mobile","description":"手机号","require":"true","type":"string","testValue":""}],"description":"发送手机验证码","contentType":"JSON","url":"$host1$/m/sms/sendSmsCode","responseArgs":[{"children":[],"name":"succeed","description":"成功标志","require":"true","type":"string","testValue":""},{"children":[],"name":"msg","description":"返回消息","require":"true","type":"string","testValue":""},{"children":[],"name":"data","description":"{}","require":"true","type":"string","testValue":""}],"example":"","status":"有效"}
         * id : 1xWbb3e15
         * name : 手机验证码发送
         * parentId : 1xUO1z7tM
         * projectId : FOQnzXzFV
         * sort : 3
         * type : sys.http
         */

        private String content;
        private String id;
        private String name;
        private String parentId;
        private String projectId;
        private int sort;
        private String type;

        public List<ChildrenEntity> getChildren() {
            return children;
        }

        public void setChildren(List<ChildrenEntity> children) {
            this.children = children;
        }

        private List<ChildrenEntity> children;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
}
