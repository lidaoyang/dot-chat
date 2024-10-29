package com.dot.comm.em;

/**
 * 用户角色
 *
 * @author: Dao-yang.
 * @date: Created in 2023/2/6 11:41
 */
public enum UserRoleEm {
    
    VISITOR("0", "游客"),
    STAFF("1", "员工"),
    VIP("3", "会员"),
    PURCHASER("2", "采购员");
    
    private String id;
    
    private String desc;
    
    UserRoleEm(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getDescByCode(String id) {
        for (UserRoleEm em : UserRoleEm.values()) {
            if (em.getId().equals(id)) {
                return em.getDesc();
            }
        }
        return "other";
    }
    
    public static UserRoleEm getByCode(String id) {
        for (UserRoleEm em : UserRoleEm.values()) {
            if (em.getId().equals(id)) {
                return em;
            }
        }
        return VISITOR;
    }
    
    /**
     * 是否是游客
     *
     * @param id 角色ID
     * @return true/false
     */
    public static boolean isVisitor(String id) {
        return UserRoleEm.VISITOR.getId().equals(id);
    }
    
    /**
     * 是否是会员/员工
     *
     * @param id 角色ID
     * @return true/false
     */
    public static boolean isVipAStaff(String id) {
        return UserRoleEm.STAFF.getId().equals(id) || UserRoleEm.VIP.getId().equals(id);
    }
    
    /**
     * 是否是会员/员工
     *
     * @param id 角色ID
     * @return true/false
     */
    public static boolean isVip(String id) {
        return UserRoleEm.VIP.getId().equals(id);
    }
    
    /**
     * 是否是采购员
     *
     * @param id 角色ID
     * @return true/false
     */
    public static boolean isPurchaser(String id) {
        return UserRoleEm.PURCHASER.getId().equals(id);
    }
    
}
