# Root Cause Analysis Response
## DIAL System Data Discrepancy Investigation

**Date:** January 12, 2026  
**To:** Rick, Eddie  
**From:** EA Modernization Team  
**Subject:** Response to Data Discrepancy Findings

---

Thank you for your detailed analysis and follow-through on the identified discrepancies. We have completed our investigation into each of the five issues raised. Below is our root cause analysis and findings for each item.

---

## Issue 1: 12/29/25 – EB Mismatch

**Root Cause:** Golden Gate Synchronization Timing

**Investigation:**

Splunk log analysis confirms the EB load source file count is **375,965 records**, which matches the Legacy system count after EB processing completes.

The apparent mismatch (175,816 vs 373,965) observed in the comparison occurs because the Modernization EB process includes post-load logic that:

1. Updates EBSID values based on ENT table data
2. Deletes records where EBSID is NULL

This is expected behavior per the original legacy design.

**Resolution:**

The difference is attributed to Golden Gate sync timing. When comparing data, ensure both systems have completed their respective processing cycles. The underlying data transformation logic is functioning correctly.

---

## Issue 2: 11/10/25 – Missing Extracts (Except E5)

**Root Cause:** Unable to Determine – Insufficient Log Data

**Investigation:**

Splunk log review indicates no log entries exist prior to December 8, 2025. This prevents us from determining the specific cause of the 11/10/25 and 11/12/25 failures.

Possible contributing factors include:
- Database connectivity issues during that period
- Infrastructure or environment issues
- Source file delivery problems

**Resolution:**

Due to log retention limitations, a definitive root cause cannot be established for this historical issue. We recommend focusing on ensuring current monitoring and alerting mechanisms are in place to detect similar issues promptly.

---

## Issue 3: sanity.sql Script Failures

**Root Cause:** Golden Gate Synchronization Issue

**Investigation:**

The 11 differences identified in the sanity.sql comparison (codes 603, 605, 611, 613, 621, 711, 750, 760, 770, 809, 810) are attributed to Golden Gate replication timing.

These discrepancies represent minor count variations that occur when the sanity check runs before Golden Gate replication has fully synchronized the data between environments.

**Resolution:**

Investigation is ongoing to determine optimal timing for sanity checks relative to Golden Gate sync completion. We are evaluating options to either delay sanity checks or implement sync verification prior to validation.

---

## Issue 4: 12/24/25 – Missing Holiday Rows

**Root Cause:** Holiday Configuration Difference – Working as Designed

**Investigation:**

The Legacy system treated both December 24 and December 25 as holidays. However, verification of the M7 ALS HOLIDAYDETAILS table confirms that December 24, 2025 is **NOT** configured as a holiday.

**Validation Query:**
```sql
SELECT DATELIB.xtrcthldy('12/24/2025') FROM dual;
```

**Result:** `1/1/1900` (indicates NOT a holiday)

The HOLIDAYDETAILS table shows Christmas Day (December 25) is configured as "Always December 25th" but December 24 (Christmas Eve) is not included in the official holiday list.

**Resolution:**

The Modernization system correctly processed data on 12/24/25 because it is not defined as a holiday in the authoritative HOLIDAYDETAILS table. The daily load ran as expected, anticipating source file delivery. This is working as designed per the legacy holiday logic configuration.

---

## Issue 5: Missing Email Notifications

**Root Cause:** Email Alerting Not Configured in Splunk

**Investigation:**

Email notification functionality has not been configured in the Splunk monitoring environment. This limitation has been previously communicated to the stakeholder team.

**Resolution:**

Email alerting setup requires coordination with the infrastructure team and is outside the current project scope. We recommend submitting a formal request to the infrastructure team if automated email notifications are a priority requirement.

---

## Summary

| # | Issue | Root Cause | Status |
|---|-------|------------|--------|
| 1 | EB Mismatch (12/29/25) | Golden Gate Sync Timing | Explained |
| 2 | Missing Extracts (11/10/25) | Insufficient Log Data | Cannot Determine |
| 3 | sanity.sql Failures | Golden Gate Sync Issue | Under Investigation |
| 4 | Missing Holiday Rows (12/24/25) | Not a Configured Holiday | Working as Designed |
| 5 | No Email Notifications | Not Configured | Known Limitation |

---

We appreciate the thorough analysis and collaborative approach to identifying these issues. Please let us know if you require any additional information or clarification on the findings above.

Best regards,  
**EA Modernization Team**
