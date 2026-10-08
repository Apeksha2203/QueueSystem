// VIVA GUIDE: Pure animation layout helper. Groups large queues visually; it does not calculate authoritative server queue order.
// Normalize the people-ahead count and calculate illustration coordinates; grouped people remain represented by the hidden count.
export function queueLayout(value) {
  const count = Number(value);
  const ahead = Number.isFinite(count) ? Math.max(0, Math.trunc(count)) : 0;
  const grouped = ahead > 4;
  const shown = grouped ? 3 : ahead;
  return {
    ahead,
    grouped,
    hidden: ahead - shown,
    you: grouped ? 55 : 342 - shown * 66,
    people: Array.from({ length: shown }, (_, index) => 342 - index * (grouped ? 56 : 66)),
  };
}
