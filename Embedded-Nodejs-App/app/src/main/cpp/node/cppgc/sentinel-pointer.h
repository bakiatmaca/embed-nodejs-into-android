// Copyright 2021 the V8 project authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef INCLUDE_CPPGC_SENTINEL_POINTER_H_
#define INCLUDE_CPPGC_SENTINEL_POINTER_H_

#include <cstdint>

namespace cppgc {
namespace internal {

// Special tag type used to denote some sentinel member. The semantics of the
// sentinel is defined by the embedder.
struct SentinelPointer {
  template <typename T>
  operator T*() const {
    static constexpr intptr_t kSentinelValue = 1;
    return reinterpret_cast<T*>(kSentinelValue);
  }
  // Hidden friends.
  friend bool operator==(SentinelPointer, SentinelPointer) { return true; }
  friend bool operator!=(SentinelPointer, SentinelPointer) { return false; }
};

}  // namespace internal

constexpr internal::SentinelPointer kSentinelPointer;

}  // namespace cppgc

#endif  // INCLUDE_CPPGC_SENTINEL_POINTER_H_
