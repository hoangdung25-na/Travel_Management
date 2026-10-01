import { ButtonHTMLAttributes, forwardRef } from "react";
import { Loader2 } from "lucide-react";
import clsx from "clsx";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "ghost" | "danger";
  loading?: boolean;
}

const variants = {
  primary: "bg-[#E2603F] text-white hover:bg-[#d55333] shadow-sm",
  secondary: "bg-[#0D2B2B] text-[#D4A24C] hover:bg-[#123A3A]",
  ghost: "bg-transparent text-stone-800 border border-[#e7e0d3] hover:bg-[#f5f0e6]",
  danger: "bg-white text-[#E2603F] border border-[#E2603F]/40 hover:bg-[#E2603F]/5",
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", loading, disabled, children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        disabled={disabled || loading}
        className={clsx(
          "inline-flex items-center justify-center gap-2 rounded-full px-5 py-2.5 text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed",
          variants[variant],
          className
        )}
        {...props}
      >
        {loading && <Loader2 size={16} className="animate-spin" />}
        {children}
      </button>
    );
  }
);
Button.displayName = "Button";
