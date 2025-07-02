package catering.domains.holidayleave.domain;

import catering.domains.staffmember.domain.StaffMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayLeave {

    public enum RequestStatus {
        ACCETTATA,
        RIFIUTATA,
        IN_ATTESA
    }

    private int id;
    private StaffMember staffMember;
    private Date startDate;
    private Date endDate;
    private RequestStatus status = RequestStatus.IN_ATTESA;

    public HolidayLeave(StaffMember staffMember, Date startDate, Date endDate, RequestStatus status) {
        this.staffMember = staffMember;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public void accept() {
        this.status = RequestStatus.ACCETTATA;
    }

    public void reject() {
        this.status = RequestStatus.RIFIUTATA;
    }

    public boolean isPending() {
        return this.status == RequestStatus.IN_ATTESA;
    }

    @Override
    public String toString() {
        return String.format("HolidayLeave{id=%d, staff=%s %s, from=%s to=%s, status=%s}",
                id,
                staffMember != null ? staffMember.getName() : "Unknown",
                staffMember != null ? staffMember.getSurname() : "Unknown",
                startDate,
                endDate,
                status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof HolidayLeave))
            return false;

        HolidayLeave other = (HolidayLeave) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
