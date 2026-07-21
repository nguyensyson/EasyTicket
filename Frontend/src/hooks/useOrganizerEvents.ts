import { useContext } from "react";
import { OrganizerEventContext } from "@/context/OrganizerEventContext";

export function useOrganizerEvents() {
  const ctx = useContext(OrganizerEventContext);
  if (!ctx) {
    throw new Error(
      "useOrganizerEvents must be used within OrganizerEventProvider",
    );
  }
  return ctx;
}
