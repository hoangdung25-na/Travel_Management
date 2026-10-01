export function TourCardSkeleton() {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white overflow-hidden animate-pulse flex flex-col h-full">
      <div className="h-48 w-full bg-slate-200" />
      <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
        <div>
          <div className="h-4 w-1/4 bg-slate-200 rounded mb-2" />
          <div className="h-5 w-4/5 bg-slate-200 rounded mb-2" />
          <div className="h-5 w-3/5 bg-slate-200 rounded" />
        </div>
        <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
          <div className="h-4 w-20 bg-slate-200 rounded" />
          <div className="h-6 w-24 bg-slate-200 rounded" />
        </div>
      </div>
    </div>
  );
}
