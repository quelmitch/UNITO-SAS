package catering.domains.staffmember.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@NoArgsConstructor
public class StaffMember {
    @Getter
    public enum Role {
        CUOCO(0),
        CHEF(1),
        ORGANIZZATORE(2),
        SERVIZIO(3),
        PROPRIETARIO(4);

        private final int roleId;

        Role(int roleId) {
            this.roleId = roleId;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public enum EmploymentType {
        PERMANENTE,
        OCCASIONALE
    }

    private int id;
    private String email;
    private String name;
    private String surname;
    private Date dateOfBirth;
    private String address;
    private String phone;
    private int wage;
    private EmploymentType employmentType;
    private final Map<Role, Set<String>> roles = new HashMap<>();

    public StaffMember(String email, String name, String surname, Date dateOfBirth, String address,
                       String phone, int wage, EmploymentType employmentType) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.wage = wage;
        this.employmentType = employmentType;
    }

    public Map<Role, Set<String>> getRoles() {
        return new HashMap<>(this.roles);
    }

    public Map<Role, Set<String>> getInternalRolesMap() {
        return this.roles;
    }


    // BUSINESS LOGIC

    public boolean hasRole(Role role) {
        return this.roles.containsKey(role);
    }

    public boolean addRole(Role role, Set<String> jobs) {
        return this.roles.computeIfAbsent(role, k -> new HashSet<>()).addAll(jobs);
    }

    public boolean removeRole(Role role) {
        return this.roles.remove(role) != null;
    }

    public boolean removeJobs(Role role, Set<String> jobs) {
        return this.roles.get(role) != null && this.roles.get(role).removeAll(jobs);
    }

    public void changeEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(" ").append(email);

        if (!roles.isEmpty()) {
            sb.append(" : ");
            for (Map.Entry<Role, Set<String>> entry : roles.entrySet()) {
                Role role = entry.getKey();
                Set<String> associatedData = entry.getValue();

                sb.append(role.toString()).append(" [");
                if (!associatedData.isEmpty()) {
                    sb.append(String.join(", ", associatedData));
                }
                sb.append("] ");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        StaffMember other = (StaffMember) obj;

        // If both staff members have valid IDs, compare by ID
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Otherwise, if either ID is 0, compare by email
        return this.email != null && this.email.equals(other.email);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        // Use ID if it's valid
        if (id > 0) {
            result = prime * result + id;
        } else {
            // Otherwise use email
            result = prime * result + (email != null ? email.hashCode() : 0);
        }

        return result;
    }
}
