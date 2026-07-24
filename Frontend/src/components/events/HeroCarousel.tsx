import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { EventItem } from "@/types/event";
import { getEventCategoryDisplayLabel, getEventImageStyle } from "@/utils/eventImage";

const AUTOPLAY_MS = 5000;

export function HeroCarousel({ events }: { events: EventItem[] }) {
  const [index, setIndex] = useState(0);
  const [paused, setPaused] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (paused || events.length <= 1) return;
    const timer = setInterval(() => {
      setIndex((i) => (i + 1) % events.length);
    }, AUTOPLAY_MS);
    return () => clearInterval(timer);
  }, [paused, events.length]);

  if (events.length === 0) return null;

  return (
    <section
      className="relative h-[300px] overflow-hidden sm:h-[360px] lg:h-[440px]"
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
    >
      {events.map((slide, i) => (
        <div
          key={slide.id}
          className="absolute inset-0 flex items-center transition-opacity duration-[600ms] ease-in-out"
          style={{
            opacity: i === index ? 1 : 0,
            pointerEvents: i === index ? "auto" : "none",
          }}
        >
          {slide.bannerUrl ? (
            <img
              src={slide.bannerUrl}
              alt={slide.title}
              className="absolute inset-0 h-full w-full object-cover"
            />
          ) : (
            <div className="absolute inset-0" style={getEventImageStyle(slide.category)} />
          )}
          <div
            className="absolute inset-0"
            style={{
              background:
                "linear-gradient(90deg,rgba(20,30,24,0.82) 0%,rgba(20,30,24,0.5) 55%,rgba(20,30,24,0.15) 100%)",
            }}
          />
          <div className="relative z-[1] max-w-[480px] px-4 sm:px-10 lg:px-16">
            <div className="mb-4 inline-block rounded-pill bg-gold px-3.5 py-1.5 text-xs font-bold text-dark">
              {getEventCategoryDisplayLabel(slide)}
            </div>
            <h1 className="mb-2.5 text-2xl font-extrabold leading-tight text-cream sm:text-[34px]">
              {slide.title}
            </h1>
            <p className="mb-5 text-sm text-[#DCE6DF]">
              {slide.dateLabel} · {slide.venue}, {slide.city}
            </p>
            <button
              onClick={() => navigate(`/events/${slide.id}`)}
              className="cursor-pointer rounded-[10px] bg-gold px-6.5 py-3.5 text-[15px] font-bold text-dark hover:bg-gold-hover"
            >
              Mua vé ngay
            </button>
          </div>
        </div>
      ))}

      <div className="absolute inset-x-0 bottom-5 z-[2] flex justify-center gap-2">
        {events.map((slide, i) => (
          <button
            key={slide.id}
            aria-label={`Xem slide ${i + 1}`}
            onClick={() => setIndex(i)}
            className="h-2 w-2 cursor-pointer rounded-full transition-colors"
            style={{
              background: i === index ? "#D9B26F" : "rgba(245,241,232,0.5)",
            }}
          />
        ))}
      </div>
    </section>
  );
}
